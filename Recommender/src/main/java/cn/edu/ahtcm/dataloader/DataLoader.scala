package cn.edu.ahtcm.dataloader

import cn.edu.ahtcm.caseclass.bean.{Product, Rating}
import cn.edu.ahtcm.utils.DBUtils.{config, writeToDB}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object DataLoader {

  // 定义数据文件路径
  // val PRODUCT_DATA_PATH = "Recommender/src/main/resources/products.csv"
  // val RATING_DATA_PATH = "Recommender/src/main/resources/ratings.csv"

  val PRODUCT_DATA_PATH = "/root/products.csv"
  val RATING_DATA_PATH = "/root/ratings.csv"

  // 定义数据库中存储的表名
  val PRODUCT = "product"
  val RATING = "rating"

  def main(args: Array[String]): Unit = {

    // 创建一个 SparkConf
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("DataLoader")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._

    val productRDD = spark.sparkContext.textFile(PRODUCT_DATA_PATH)
    val productDF = productRDD.map(line => {
      val attr = line.split("\\^")
      Product(attr(0).toInt, attr(1).trim, attr(4).trim, attr(5).trim, attr(6).trim)
    }).toDF()

    val ratingRDD = spark.sparkContext.textFile(RATING_DATA_PATH)
    val ratingDF = ratingRDD.map(line => {
      val attr = line.split(",")
      Rating(attr(0).toInt, attr(1).toInt, attr(2).toDouble, attr(3).toInt)
    }).toDF()

    writeToDB(productDF, PRODUCT)
    writeToDB(ratingDF, RATING)

    spark.stop()
  }

}
