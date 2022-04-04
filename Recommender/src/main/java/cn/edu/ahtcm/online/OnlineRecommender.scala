package cn.edu.ahtcm.online

import cn.edu.ahtcm.caseclass.bean.Rating
import cn.edu.ahtcm.caseclass.recs.ProductRecs
import cn.edu.ahtcm.utils.DBUtils.{config, dbConnect, getProp}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Jedis

import java.sql.DriverManager

object OnlineRecommender {

  // 定义数据库中存储的表名
  val RATING = "rating"
  val STREAM_RECS = "stream_recs"
  val PRODUCT_RECS = "product_recs"

  // 定义最大推荐个数字
  val MAX_USER_RATING = 20
  val MAX_SIM_PRODUCTS = 20

  def main(args: Array[String]): Unit = {

    // 懒变量定义，使用时初始化
    lazy val jedis = new Jedis("192.168.10.128", 6379)

    val kafkaConfig = Map(
      "kafka.topic" -> "recommender"
    )

    // 创建 spark conf
    val sparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("OnlineRecommender")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()
    val sc = spark.sparkContext
    val ssc = new StreamingContext(sc, Seconds(2))

    import spark.implicits._

    val ratingMap = spark.read
      .format("jdbc")
      .options(dbConnect(RATING))
      .load()
      .as[Rating]
      .rdd
      .map(x => (x.userId, (x.productId)))
      .groupByKey()
      .collect()
      .map(x => (x._1, x._2.toArray))
      .toMap

    // 加载数据 相似度矩阵 广播出去
    val simProductsMatrix = spark.read
      .format("jdbc")
      .options(dbConnect(PRODUCT_RECS))
      .load()
      .as[ProductRecs]
      .rdd
      .groupBy(_.productId)
      // 为了后续查询相似度方便，把数据转换成map形式
      .map { item =>
        (item._1, item._2.map(x => (x.productId, x.score)).toMap)
      }
      .collectAsMap()

    // 定义广播变量
    val simProcutsMatrixBC = sc.broadcast(simProductsMatrix)

    // 创建 kafka 配置参数
    val kafkaParam = Map(
      "bootstrap.servers" -> "localhost:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "recommender",
      "auto.offset.reset" -> "latest"
    )

    // 创建一个 DStream
    val kafkaStream = KafkaUtils.createDirectStream[String, String](ssc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](Array(kafkaConfig("kafka.topic")), kafkaParam)
    )

    // 对 kafkaStream 进行处理，产生评分流: userId|productId|score|timestamp
    val ratingStream = kafkaStream.map { message =>
      var attr = message.value().split("\\|")
      (attr(0).toInt, attr(1).toInt, attr(2).toDouble, attr(3).toInt)
    }

    // 核心算法部分，定义评分流的处理流程
    ratingStream.foreachRDD {
      rdds =>
        rdds.foreach {
          case (userId, productId, score, timestamp) =>
            print("rating data coming! ==> ")
            println(userId, productId, score, timestamp)

            // 1. 从redis里取出当前用户的最近评分，保存成一个数组 Array[(productId, score)]
            val userRecentlyRatings = getUserRecentlyRatings(MAX_USER_RATING, userId, jedis)

            // 2. 从相似度矩阵中获取当前商品最相似的商品列表，作为备选列表，保存成一个数组Array[productId]
            val candidateProducts = getTopSimProducts(MAX_SIM_PRODUCTS, productId, userId, simProcutsMatrixBC.value, ratingMap)

            // 3. 计算每个备选商品的推荐优先级，得到当前用户的实时推荐列表，保存成 Array[(productId, score)]
            val streamRecs = computeProductScore(candidateProducts, userRecentlyRatings, simProcutsMatrixBC.value)

            // 4. 把推荐列表保存到mongodb
            saveDataToMongoDB(userId, streamRecs)
        }
    }

    // 启动 streaming
    ssc.start()
    println("========== streaming started! ==========")
    ssc.awaitTermination()
  }

  import scala.collection.JavaConversions._

  /**
   * 从 redis 中获取当前用户的最近评分
   *
   * @param num    推荐个数字
   * @param userId 用户 ID
   * @param jedis  redis 连接
   * @return 当前用户的最近评分列表
   */
  def getUserRecentlyRatings(num: Int, userId: Int, jedis: Jedis): Array[(Int, Double)] = {
    // 从redis中用户的评分队列里获取评分数据，list 键名为 uid:USERID，值格式是 PRODUCTID:SCORE
    jedis.lrange("userId:" + userId.toString, 0, num)
      .map { item =>
        val attr = item.split("\\:")
        (attr(0).trim.toInt, attr(1).trim.toDouble)
      }
      .toArray
  }

  /**
   * 获取当前商品的相似列表，并过滤用户已评分的，作为备选列表
   *
   * @param num         推荐个数字
   * @param productId   商品 ID
   * @param userId      用户 ID
   * @param simProducts 相似矩阵
   * @return 当前商品的相似列表
   */
  def getTopSimProducts(num: Int, productId: Int, userId: Int,
                        simProducts: scala.collection.Map[Int, scala.collection.immutable.Map[Int, Double]],
                        ratingMap: Map[Int, Array[Int]]): Array[Int] = {

    // 从广播变量相似度矩阵中拿到当前商品的相似度列表
    val allSimProducts = simProducts(productId).toArray

    // 获得用户已经评分过的商品，过滤掉，排序输出
    val ratingExist = ratingMap(userId)

    // 从所有的相似商品中进行过滤
    allSimProducts.filter(x => !ratingExist.contains(x._1))
      .sortWith(_._2 > _._2)
      .take(num)
      .map(x => x._1)
  }

  /**
   * 计算每个备选商品的推荐得分
   *
   * @param candidateProducts   相似的商品列表
   * @param userRecentlyRatings 当前商品的相似列表
   * @param simProducts         相似矩阵
   * @return 每个备选商品的推荐得分列表
   */
  def computeProductScore(candidateProducts: Array[Int], userRecentlyRatings: Array[(Int, Double)],
                          simProducts: scala.collection.Map[Int, scala.collection.immutable.Map[Int, Double]]): Array[(Int, Double)] = {

    // 定义一个长度可变数组ArrayBuffer，用于保存每一个备选商品的基础得分，(productId, score)
    val scores = scala.collection.mutable.ArrayBuffer[(Int, Double)]()

    // 定义两个map，用于保存每个商品的高分和低分的计数器，productId -> count
    val increMap = scala.collection.mutable.HashMap[Int, Int]()
    val decreMap = scala.collection.mutable.HashMap[Int, Int]()

    // 遍历每个备选商品，计算和已评分商品的相似度
    for (candidateProduct <- candidateProducts; userRecentlyRating <- userRecentlyRatings) {
      // 从相似度矩阵中获取当前备选商品和当前已评分商品间的相似度
      val simScore = getProductsSimScore(candidateProduct, userRecentlyRating._1, simProducts)
      println("相似度 =>" + simScore)
      if (simScore > 0.4) {
        // 按照公式进行加权计算，得到基础评分
        scores += ((candidateProduct, simScore * userRecentlyRating._2))
        if (userRecentlyRating._2 > 3) {
          increMap(candidateProduct) = increMap.getOrDefault(candidateProduct, 0) + 1
        } else {
          decreMap(candidateProduct) = decreMap.getOrDefault(candidateProduct, 0) + 1
        }
      }
    }

    // 根据公式计算所有的推荐优先级，首先以 productId 做 groupBy
    scores.groupBy(_._1).map {
      case (productId, scoreList) =>
        (productId, scoreList.map(_._2).sum / scoreList.length + log(increMap.getOrDefault(productId, 1)) - log(decreMap.getOrDefault(productId, 1)))
    }
      // 返回推荐列表，按照得分排序
      .toArray
      .sortWith(_._2 > _._2)
  }

  /**
   * 从相似度矩阵中获取备选商品和当前已评分商品的相似度
   *
   * @param product1    相似的商品
   * @param product2    当前商品的相似
   * @param simProducts 相似矩阵
   * @return 相似度
   */
  def getProductsSimScore(product1: Int, product2: Int,
                          simProducts: scala.collection.Map[Int, scala.collection.immutable.Map[Int, Double]]): Double = {
    simProducts.get(product1) match {
      case Some(sims) => sims.get(product2) match {
        case Some(score) => score
        case None => 0.0
      }
      case None => 0.0
    }
  }

  /**
   * 自定义 log 函数
   *
   * @param num 被 log 的数
   * @return log 后的数
   */
  def log(num: Int): Double = {
    val N = 10
    math.log(num) / math.log(N)
  }

  /**
   * 保存到 MySQL
   *
   * @param userId     用户 ID
   * @param streamRecs 每个备选商品的推荐优先级列表
   */
  def saveDataToMongoDB(userId: Int, streamRecs: Array[(Int, Double)]): Unit = {

    val connection = DriverManager.getConnection(config("mysql.url"), getProp)
    var sql = "delete from " + STREAM_RECS + " where userId = " + userId
    connection.prepareStatement(sql).executeUpdate()

    streamRecs.flatMap(item => {
      sql = "insert into " + STREAM_RECS + " (userId, productId, score) values (" + userId + ", " + item._1 + ", " + item._2 + ")"
      connection.prepareStatement(sql).executeUpdate()
      None
    })
    connection.close()
  }
}
