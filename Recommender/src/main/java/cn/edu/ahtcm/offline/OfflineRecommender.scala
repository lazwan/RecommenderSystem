package cn.edu.ahtcm.offline

import cn.edu.ahtcm.caseclass.bean.ProductRating
import cn.edu.ahtcm.caseclass.recs.{ProductRecs, UserRecs}
import cn.edu.ahtcm.utils.DBUtils.{config, dbConnect, writeToDB}
import org.apache.spark.SparkConf
import org.apache.spark.mllib.recommendation.{ALS, Rating}
import org.apache.spark.sql.SparkSession
import org.jblas.DoubleMatrix

object OfflineRecommender {

  // 定义数据库中存储的表名
  val RATING = "rating"
  val USER_RECS = "user_recs"
  val PRODUCT_RECS = "product_recs"

  // 定义最大推荐个数字
  val USER_MAX_RECOMMENDATION = 20

  def main(args: Array[String]): Unit = {

    // 创建一个 SparkConf
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("OfflineRecommender")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._

    val ratingRDD = spark.read
      .format("jdbc")
      .options(dbConnect(RATING))
      .load()
      .as[ProductRating]
      .rdd
      .map(rating => (rating.userId, rating.productId, rating.score))
      .cache()

    // 提取出用户和商品的数据集
    val userRDD = ratingRDD.map(_._1).distinct()
    val productRDD = ratingRDD.map(_._2).distinct()

    // 1. 训练隐语义模型
    val trainData = ratingRDD.map(x => Rating(x._1, x._2, x._3))
    // rank 是模型中隐语义因子的个数, iterations 是迭代的次数, lambda 是ALS的正则化参
    val (rank, iterations, lambda) = (10, 10, 0.5) // 调用ALS算法训练隐语义模型
    val model = ALS.train(trainData, rank, iterations, lambda)

    // 2. 获得评分矩阵，得到用户的推荐列表
    // userRDD 和 productRDD 做笛卡尔积，得到 userProductsRDD
    val userProducts = userRDD.cartesian(productRDD)
    val preRatings = model.predict(userProducts)

    // 从预测评分矩阵中提取到用户的推荐列表
    val userRecs = preRatings.filter(_.rating > 0)
      .map(rating => (rating.user, (rating.product, rating.rating)))
      .groupByKey()
      .flatMap(line => {
        val topItem = line._2.toArray.sortWith(_._2 > _._2).take(USER_MAX_RECOMMENDATION)
        topItem.map(item => UserRecs(line._1, item._1, item._2))
      })
      .toDF()


    // 存储到数据库
    writeToDB(userRecs, USER_RECS)

    // 3. 利用商品的特征向量，计算商品相似度列表
    val productFeatures = model.productFeatures.map {
      case (productId, features) => (productId, new DoubleMatrix(features))
    }

    // 两两配对商品，计算余弦相似度
    val productRecs = productFeatures.cartesian(productFeatures)
      .filter { case (a, b) => a._1 != b._1 }
      .map {
        case (a, b) =>
          // 求余弦相似度
          val similarScore = cosineSim(a._2, b._2)
          (a._1, (b._1, similarScore))
      }
      .filter(_._2._2 > 0.4)
      .groupByKey()
      .flatMap(line => {
        val topItem = line._2.toArray.sortWith(_._2 > _._2).take(USER_MAX_RECOMMENDATION)
        topItem.map(item => ProductRecs(line._1, item._1, item._2))
      })
      .toDF()

    // 存储到数据库
    writeToDB(productRecs, PRODUCT_RECS)

    spark.stop()
  }

  /**
   * 计算两个商品之间的余弦相似度
   *
   * @param product1 商品 1 特征向量矩阵
   * @param product2 商品 2 特征向量矩阵
   * @return
   */
  def cosineSim(product1: DoubleMatrix, product2: DoubleMatrix): Double = {
    product1.dot(product2) / (product1.norm2() * product2.norm2())
  }

}
