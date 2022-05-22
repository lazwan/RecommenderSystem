package cn.edu.ahtcm.utils

import org.apache.spark.sql.{DataFrame, SaveMode}

import java.util.Properties

object DBUtils {

  def config: Map[String, String] = {
    Map(
      "mysql.url" -> "jdbc:mysql://master:3306/recommender?useUnicode=true&characterEncoding=utf8",
      "spark.cores" -> "spark://master:7077"
    )
  }

  def getProp: Properties ={
    val prop = new Properties()

    prop.put("user", "lazywa")
    prop.put("password", "12345678")
    prop.put("driver", "com.mysql.cj.jdbc.Driver")
    // prop.put("createTableColumnTypes", "CHARACTER SET utf8")

    prop
  }

  def dbConnect(table: String): Map[String, String] = {
    val options = Map[String, String](
      elems = "url" -> "jdbc:mysql://master:3306/recommender?useUnicode=true&characterEncoding=utf8",
      "driver" -> "com.mysql.cj.jdbc.Driver",
      "user" -> "lazywa",
      "password" -> "12345678",
      "dbtable" -> table
    )
    options
  }

  def writeToDB(df: DataFrame, table: String): Unit = {

    df.write
      .mode(SaveMode.Overwrite)
      .jdbc("jdbc:mysql://master:3306/recommender?useUnicode=true&characterEncoding=utf8", table, getProp)

  }



}
