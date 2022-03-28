package cn.edu.ahtcm.offline

import breeze.numerics.sqrt
import cn.edu.ahtcm.caseclass.bean.ProductRating
import cn.edu.ahtcm.utils.DBUtils.{config, dbConnect}
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.mllib.recommendation.{ALS, MatrixFactorizationModel, Rating}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

object ALSTrainer {

  // 定义数据库中存储的表名
  val RATING = "rating"

  private val logger: Logger = Logger.getLogger(ALSTrainer.getClass)

  def main(args: Array[String]): Unit = {

    // 创建一个 SparkConf
    val sparkConf: SparkConf = new SparkConf().setMaster(config("spark.cores")).setAppName("OfflineRecommender")
    val spark = SparkSession.builder().config(sparkConf).getOrCreate()

    import spark.implicits._

    // 加载数据
    val ratingRDD = spark.read
      .format("jdbc")
      .options(dbConnect(RATING))
      .load()
      .as[ProductRating]
      .rdd
      .map(rating => Rating(rating.userId, rating.productId, rating.score))
      .cache()

    // 数据集切分 => 训练集 + 测试集
    val splits= ratingRDD.randomSplit(Array(0.8, 0.2))
    val trainingRDD = splits(0)
    val testingRDD = splits(1)

    // 核心实现，输出最优参数
    adjustALSParams(trainingRDD, testingRDD)

    spark.stop()
  }

  def adjustALSParams(trainingData: RDD[Rating], testingData: RDD[Rating]): Unit = {
    val result = for (rank <- Array(5, 10, 15, 20, 25, 50); lambda <- Array(1, 0.5, 0.2, 0.1))
      yield {
        val model = ALS.train(trainingData, rank, 10, lambda)
        val rmse = getRMSE(model, testingData)
        (rank, lambda, rmse)
      }

    // 按照 rmse 排序并输出最优参数
    logger.info(result.minBy(_._3))
    println(result.minBy(_._3))

  }

  def getRMSE(model: MatrixFactorizationModel, data: RDD[Rating]): Double = {
    // 构建 userProducts 得到预测评分矩阵
    val userProducts = data.map(item => (item.user, item.product))
    val predictRating = model.predict(userProducts)

    // 按照公式计算 RMSE
    // 1. 首先把预测评分和实际评分按照 (userId, productId) 做一个连接
    val observed = data.map(item => ((item.user, item.product), item.rating))
    val predict = predictRating.map(item => ((item.user, item.product), item.rating))

    sqrt(observed.join(predict).map {
      case ((userId, productId), (actual, pre)) =>
        val err = actual - pre
        err * err
    }.mean())
  }
}
