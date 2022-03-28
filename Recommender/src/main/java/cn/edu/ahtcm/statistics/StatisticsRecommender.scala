package cn.edu.ahtcm.statistics

import cn.edu.ahtcm.caseclass.bean.Rating
import cn.edu.ahtcm.utils.DBUtils.{config, dbConnect, writeToDB}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

import java.text.SimpleDateFormat
import java.util.Date

object StatisticsRecommender {

  // 定义数据库中存储的表名
  val RATING = "rating"
  // 历史热门商品
  val RATE_MORE_PRODUCTS = "rate_more_products"
  // 近期热门商品
  val RATE_MORE_RECENTLY_PRODUCTS = "rate_more_recently_products"
  // 商品的平均评分
  val AVERAGE_PRODUCTS = "average_products"

  def main(args: Array[String]): Unit = {

    // 创建一个 SparkConf
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("StatisticsRecommender")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._

    // 加载数据
    val ratingDF = spark.read
      .format("jdbc")
      .options(dbConnect(RATING))
      .load()
      .as[Rating]
      .toDF()

    // 创建一张 ratings 的临时表
    ratingDF.createOrReplaceTempView("ratings")

    // 1. 历史热门商品，按照评分个数统计，productId, count
    val rateMoreProductsDF = spark.sql("select productId, count(productId) count from ratings group by productId order by count desc")

    // 存储到 MySQL
    writeToDB(rateMoreProductsDF, RATE_MORE_PRODUCTS)

    // 2. 近期热门商品，把时间戳转换成 yyyyMM 格式进行评分个数统计
    // 创建日期的格式化工具
    val simpleDateFormat = new SimpleDateFormat("yyyyMM")
    spark.udf.register("changeDate", (time: Int) => simpleDateFormat.format(new Date(time * 1000L)).toInt)

    // 把原始 rating 数据转换成 productId, score, yearmonth
    val ratingOfYearMonthDF = spark.sql("select productId, score, changeDate(timestamp) as yearmonth from ratings")
    ratingOfYearMonthDF.createOrReplaceTempView("rating_of_month")
    val rateMoreRecentlyProducts = spark.sql("select productId, count(yearmonth) as count, yearmonth from rating_of_month group by yearmonth, productId order by yearmonth desc, count desc")

    // 存储到 MySQL
    writeToDB(rateMoreRecentlyProducts, RATE_MORE_RECENTLY_PRODUCTS)

    // 3. 优质商品统计，商品的平均评分
    val averageProductsDF = spark.sql("select productId, avg(score) as avg from ratings group by productId order by avg desc")
    // 存储到 MySQL
    writeToDB(averageProductsDF, AVERAGE_PRODUCTS)

    spark.stop()
  }

}
