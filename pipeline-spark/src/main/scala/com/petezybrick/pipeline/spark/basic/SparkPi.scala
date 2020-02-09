
// scalastyle:off println
package com.petezybrick.pipeline.spark.basic

import org.apache.spark.SparkException
import org.apache.spark.sql.SparkSession
import scala.math.random


/** Computes an approximation to pi */
object SparkPi {
  def main(args: Array[String]) {
    val builder = SparkSession
      .builder
      .appName("SparkPi")
    if( args.length > 1 && "local".equals(args(1))) {
      builder.config("spark.master", "local")
    }

    val sparkSession:SparkSession = builder.getOrCreate()

    val slices = if (args.length > 0) args(0).toInt else 2
    val n = math.min(100000L * slices, Int.MaxValue).toInt // avoid overflow
    val count = sparkSession.sparkContext.parallelize(1 until n, slices).map { i =>
      val x = random * 2 - 1
      val y = random * 2 - 1
      if (x * x + y * y <= 1) 1 else 0
    }.reduce(_ + _)
    println(s"Pete Z's Pi is roughly ${4.0 * count / (n - 1)}")
    sparkSession.stop()
  }
}

// scalastyle:on println
