package cn.edu.ahtcm.itemcf

import cn.edu.ahtcm.caseclass.Recommendation
import cn.edu.ahtcm.caseclass.bean.ProductRating
import cn.edu.ahtcm.caseclass.recs.ProductRecs
import cn.edu.ahtcm.utils.DBUtils.{config, dbConnect, writeToDB}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object ItemCFRecommender {

  // 定义数据库中存储的表名
  val RATING = "rating"
  val ITEM_CF_PRODUCT_RECS = "itemCF_product_recs"
  val MAX_RECOMMENDATION = 10

  def main(args: Array[String]): Unit = {

    // 创建一个spark config
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("ItemCFRecommender")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._

    // 加载数据，转换成 DF 进行处理
    val ratingDF = spark.read
      .format("jdbc")
      .options(dbConnect(RATING))
      .load()
      .as[ProductRating]
      .map(x => (x.userId, x.productId, x.score))
      .toDF("userId", "productId", "score")
      .cache()

    // 核心算法，计算同现相似度，得到商品的相似列表

    // 统计每个商品的评分个数，按照 productId 做 group by
    val productRatingCountDF = ratingDF.groupBy("productId").count()
    // 在原有的评分表上 rating 添加 count
    val ratingWithCountDF = ratingDF.join(productRatingCountDF, "productId")

    // 将评分按照用户 ID 两两配对，统计两个商品被同一个用户评分过的次数
    val joinDF = ratingWithCountDF.join(ratingWithCountDF, "userId")
      .toDF("userId", "product1", "score1", "count1", "product2", "score2", "count2")
      .select("userId", "product1", "count1", "product2", "count2")

    // 创建一张零时表，进行查询
    joinDF.createOrReplaceTempView("join")

    // 按照 product1， product2 做 group by，统计 userId 的数量，就是两个商品同时评分的人数
    val cooccurrenceDF = spark.sql(
      """
        | select product1, product2, count(userId) as cocount, first(count1) as count1, first(count2) as count2
        | from join
        | group by product1, product2
        |""".stripMargin
    ).cache()

    // 提取需要的数据，包装成 (productId1, productId2, score)
    val simDF = cooccurrenceDF.map(row => {
      val coocSim = cooccurrenceSim(row.getAs[Long]("cocount"), row.getAs[Long]("count1"), row.getAs[Long]("count2"))
      (row.getInt(0), (row.getInt(1), coocSim))
    })
      .rdd
      .groupByKey()
      .flatMap(line => {
        val topItem = line._2.toArray
          .filter(_._1 != line._1)
          .sortWith(_._2 > _._2)
          .take(MAX_RECOMMENDATION)
        topItem.map(item => ProductRecs(line._1, item._1, item._2))
      })
      .toDF()

    // 写入数据库
    writeToDB(simDF, ITEM_CF_PRODUCT_RECS)
  }

  def cooccurrenceSim(coCount: Long, count1: Long, count2: Long): Double = {
    coCount / math.sqrt(count1 + count2)
  }

}
