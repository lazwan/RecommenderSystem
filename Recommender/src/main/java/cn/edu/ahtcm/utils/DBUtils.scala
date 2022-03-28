package cn.edu.ahtcm.utils

import org.apache.spark.sql.{DataFrame, SaveMode}

import java.util.Properties

object DBUtils {

  def config: Map[String, String] = {
    Map(
      // "spark.cores" -> "spark://192.168.10.128:7077",
      "mysql.url" -> "jdbc:mysql://192.168.10.128:3306/recommender?useUnicode=true&characterEncoding=utf8",
      "spark.cores" -> "local[*]"
    )
  }

  def getProp: Properties ={
    val prop = new Properties()

    prop.put("user", "root")
    prop.put("password", "123456")
    prop.put("driver", "com.mysql.cj.jdbc.Driver")
    prop.put("createTableColumnTypes", "CHARACTER SET utf8")

    prop
  }

  def dbConnect(table: String): Map[String, String] = {
    val options = Map[String, String](
      elems = "url" -> "jdbc:mysql://192.168.10.128:3306/recommender?useUnicode=true&characterEncoding=utf8",
      "driver" -> "com.mysql.cj.jdbc.Driver",
      "user" -> "root",
      "password" -> "123456",
      "dbtable" -> table
    )
    options
  }

  def writeToDB(df: DataFrame, table: String): Unit = {

    df.write
      .mode(SaveMode.Overwrite)
      .jdbc("jdbc:mysql://192.168.10.128:3306/recommender?useUnicode=true&characterEncoding=utf8", table, getProp)

  }



}
