/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// scalastyle:off println
package com.petezybrick.pipeline.spark.basic

import org.apache.avro.generic.GenericData
import org.apache.spark.sql.{SparkSession, _}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._


object FlightData {

  case class Flight(_id: String, dofW: Integer, carrier: String, origin: String, dest: String,
                    crsdephour: Integer, crsdeptime: Double, depdelay: Double,crsarrtime: Double, arrdelay: Double,
                    crselapsedtime: Double, dist: Double) extends Serializable


  def main(args: Array[String]) {
    val builder = SparkSession
      .builder
      .appName("Spark Flights")
    if( args.length > 0 && "local".equals(args(0))) {
      builder.config("spark.master", "local")
    }

    val sparkSession:SparkSession = builder.getOrCreate()


    val schema = StructType(Array(
      StructField("_id", StringType, true),
      StructField("dofW", IntegerType, true),
      StructField("carrier", StringType, true),
      StructField("origin", StringType, true),
      StructField("dest", StringType, true),
      StructField("crsdephour", IntegerType, true),
      StructField("crsdeptime", DoubleType, true),
      StructField("depdelay", DoubleType, true),
      StructField("crsarrtime", DoubleType, true),
      StructField("arrdelay", DoubleType, true),
      StructField("crselapsedtime", DoubleType, true),
      StructField("dist", DoubleType, true)
    ))

    var file : String = null
    if( args.length > 0 && "local".equals(args(0))) {
      file = "file://" + args(1)
    } else {
      file = "hdfs://namenode:9000/data/flight/flights20170102.json"
    }

    val before:Long = System.currentTimeMillis()
    //val df = spark.read.format("json").option("inferSchema", "false").schema(schema).load(file)
    // import sparkSession.sqlContext.implicits._
    import sparkSession.implicits._
    val df : Dataset[Flight] = sparkSession.read.format("json").option("inferSchema", "false"). schema(schema).load(file).as[Flight]
    val flightsByCarrier : KeyValueGroupedDataset[String,Flight] = df.groupByKey(flight => flight.carrier)

//    val flightArraysByCarrier : Dataset[(String, Array[Flight])] = flightsByCarrier.mapGroups { case (k, iter) => (k, iter.map(x => x).toArray) }
//    flightArraysByCarrier.foreach( kv => {
//      println("key " + kv._1)
//      println("value " + kv._2)
//    })
//    println("++++++++++++++++++++++")
//    flightArraysByCarrier.show()
    val flightDataSerDe : FlightDataSerDe = new FlightDataSerDe
    val flightSequenceByCarrier : Dataset[(String, Seq[Flight])] = flightsByCarrier.mapGroups { case (k, iter) => (k, iter.map(x => x).to) }
     flightSequenceByCarrier.foreach( kv => {
      println("key " + kv._1)
      println("value " + kv._2.size)
      kv._2.foreach( flight => {
        println(Thread.currentThread().getId + " " + flight)
        val record : GenericData.Record = flightDataSerDe.toGenericRecord(flight)
      })
    })
    println("++++++++++++++++++++++")
    //flightSequenceByCarrier.show()




//    val r1 : DataFrame = df.filter(flight => flight.carrier=="DL").select("dest","origin", "carrier").cache()
//    r1.show()
//    r1.groupBy("origin").count().orderBy(desc("origin")).show()
//
//
//    df.cache
//    df.createOrReplaceTempView("flights")
//    sparkSession.catalog.cacheTable("flights")
//    sparkSession.sql("select carrier,origin, dest, depdelay,crsdephour, " +
//      "dist, dofW from flights where depdelay > 40 order by depdelay desc limit 5").show

    println("Elapsed " + (System.currentTimeMillis()-before))
    sparkSession.stop()
  }
}

