package cn.edu.ahtcm.dataloader

import cn.edu.ahtcm.utils.DBUtils.{config, writeToDB}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object DataLoader2 {

  // 定义数据文件路径
  // val PRODUCT_DATA_PATH = "Recommender/src/main/resources/meta_Amazon_Fashion.csv"
  // val RATING_DATA_PATH = "Recommender/src/main/resources/ratings_Amazon_Fashion.csv"
  val PRODUCT_DATA_PATH = "/dataset/meta_Sports_and_Outdoors.csv"
  val RATING_DATA_PATH = "/dataset/ratings_Sports_and_Outdoors.csv"

  // 定义 MongoDB 中存储的表名
  val PRODUCT = "product"
  val RATING = "rating"

  def main(args: Array[String]): Unit = {

    // 创建一个 SparkConf
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("DataLoader")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._

    val ratingRDD = spark.sparkContext.textFile(RATING_DATA_PATH)

    val userDF = ratingRDD
      .map(line => line.split(",")(0))
      .distinct()
      .zipWithUniqueId()
      .map(line => {
        (line._1, line._2.toInt)
      })
      .toDF("userIdx", "userId")

    val productsDF = ratingRDD
      .map(line => line.split(",")(1))
      .distinct()
      .zipWithUniqueId()
      .map(line => {
        (line._1, line._2.toInt)
      })
      .toDF("productIdx", "productId")

    val ratingDF = ratingRDD
      .map(line => {
        val attr = line.split(",")
        (attr(0).trim, attr(1).trim, attr(2).toDouble, attr(3).toInt)
      })
      .toDF("userIdx", "productIdx", "score", "timestamp")
      .join(userDF, "userIdx")
      .join(productsDF, "productIdx")
      .select("userId", "productId", "score", "timestamp")

    val productRDD = spark.sparkContext.textFile(PRODUCT_DATA_PATH)
    val productDF = productRDD
      .filter(line => line.split("\\^").length == 6)
      .map(line => {
        val attr = line.split("\\^")
        (attr(0).trim, attr(1).trim, attr(2).trim, attr(3).trim, attr(4).trim)
      }).toDF("productIdx", "name", "price", "imageUrl", "categories")
      .join(productsDF, "productIdx")
      .select("productId", "name", "price", "imageUrl", "categories")

    writeToDB(productDF, PRODUCT)
    writeToDB(ratingDF, RATING)

    spark.stop()
  }
}
