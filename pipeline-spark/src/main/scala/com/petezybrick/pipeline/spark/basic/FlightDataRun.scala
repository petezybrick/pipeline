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

import java.io.IOException
import java.util.UUID

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.avro.AvroParquetWriter
import org.apache.parquet.hadoop.ParquetWriter
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.apache.spark.sql.types._
import org.apache.spark.sql.{SparkSession, _}


object FlightDataRun {

  def main(args: Array[String]) {
    val builder = SparkSession
      .builder
      .appName("Spark Flights")
    if (args.length > 0 && "local".equals(args(0))) {
      builder.config("spark.master", "local")
    }

    val sparkSession: SparkSession = builder.getOrCreate()

    var file: String = null
    if (args.length > 0 && "local".equals(args(0))) {
      file = "file://" + args(1)
    } else {
      file = "hdfs://namenode:9000/data/flight/flights20170102.json"
    }

    val flightData : FlightData = new FlightData
    val before: Long = System.currentTimeMillis()
    //val df = spark.read.format("json").option("inferSchema", "false").schema(schema).load(file)
    // import sparkSession.sqlContext.implicits._
    import sparkSession.implicits._
    val df: Dataset[Flight] = sparkSession.read.format("json").option("inferSchema", "false").schema(flightData.schema).load(file).as[Flight]
    val flightsByCarrier: KeyValueGroupedDataset[String, Flight] = df.groupByKey(flight => flight.carrier)

    //    val flightArraysByCarrier : Dataset[(String, Array[Flight])] = flightsByCarrier.mapGroups { case (k, iter) => (k, iter.map(x => x).toArray) }
    //    flightArraysByCarrier.foreach( kv => {
    //      println("key " + kv._1)
    //      println("value " + kv._2)
    //    })
    //    println("++++++++++++++++++++++")
    //    flightArraysByCarrier.show()

    //val tempPathName = System.getProperty("java.io.tmpdir") + tempFileNameExt

    val basePathName = "hdfs://pipeline-hive-namenode:9000/user/pipeline/db_pipeline/flight/p_carrier="
//    val basePathName = "pipeline-spark/docker/shared/db_pipeline/flight/p_carrier="

    val flightDataSerDe: FlightDataSerDe = new FlightDataSerDe
    val flightSequenceByCarrier: Dataset[(String, Seq[Flight])] = flightsByCarrier.mapGroups { case (k, iter) => (k, iter.map(x => x).to) }
    try {
      flightSequenceByCarrier.foreach(kv => {
        println("key " + kv._1)
        println("value " + kv._2.size)
        val ptnPathName = basePathName + kv._1 + "/" + UUID.randomUUID.toString + ".parquet"
        val writer: ParquetWriter[GenericData.Record] = createWriter(ptnPathName, flightDataSerDe.schema)
        kv._2.foreach(flight => {
          println(Thread.currentThread().getId + " " + flight)
          val record: GenericData.Record = flightDataSerDe.toGenericRecord(flight)
          writer.write(record)
        })
        writer.close()
      })
    } catch {
      case ex: Exception => {
        println(ex)
        throw new RuntimeException(ex)
      }
    }
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

    println("Elapsed " + (System.currentTimeMillis() - before))
    sparkSession.stop()
  }

  @throws[IOException]
  def createWriter(pathName: String, schemaTo: Schema): ParquetWriter[GenericData.Record] = {
    var writer = AvroParquetWriter.builder[GenericData.Record](new Path(pathName)).withSchema(schemaTo)
      .withConf(new Configuration).withCompressionCodec(CompressionCodecName.SNAPPY).build
    writer
  }
}

