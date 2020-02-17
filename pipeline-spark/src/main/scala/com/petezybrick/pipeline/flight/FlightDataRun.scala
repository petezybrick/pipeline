package com.petezybrick.pipeline.flight

import java.io.IOException
import java.sql.{Connection, ResultSet, Statement}
import java.util.UUID

import com.petezybrick.pipeline.filesystem.HdfsHelper
import com.petezybrick.pipeline.hive.HiveDataSource
import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.avro.AvroParquetWriter
import org.apache.parquet.hadoop.ParquetWriter
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.apache.spark.sql.{Dataset, KeyValueGroupedDataset, SparkSession}

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
    HiveDataSource.setJdbcParms( "jdbc:hive2://pipeline-hive-namenode:10000/db_pipeline", "", "")

    val hdfsNameNode= "pipeline-hive-namenode"
    val hadoopUser = "pipeline"
    val replaceAllFiles = true;

    val basePathName = "hdfs://" + hdfsNameNode + ":9000/user/" + hadoopUser + "/db_pipeline/flight/p_carrier="
    //    val basePathName = "pipeline-spark/docker/shared/db_pipeline/flight/p_carrier="
    val flightDataSerDe: FlightDataSerDe = new FlightDataSerDe
    val flightSequenceByCarrier: Dataset[(String, Seq[Flight])] = flightsByCarrier.mapGroups { case (k, iter) => (k, iter.map(x => x).to) }
    try {
      flightSequenceByCarrier.foreach(kv => {
        println("key " + kv._1)
        println("value " + kv._2.size)
        val hdfsHelper : HdfsHelper = new HdfsHelper(hdfsNameNode=hdfsNameNode, hadoopUser=hadoopUser)
        val connHive : Connection = HiveDataSource.getConnection()
        val stmt : Statement = connHive.createStatement()

        if( replaceAllFiles ) {
          hdfsHelper.rmDir( folder="/db_pipeline/flight/p_carrier=" + kv._1)
        }

        // TODO: write with configurable number of rows per file and/or size
        val ptnPathName = basePathName + kv._1 + "/" + UUID.randomUUID.toString + ".parquet"
        val writer: ParquetWriter[GenericData.Record] = createWriter(ptnPathName, flightDataSerDe.schema)
        kv._2.foreach(flight => {
          println(Thread.currentThread().getId + " " + flight)
          val record: GenericData.Record = flightDataSerDe.toGenericRecord(flight)
          writer.write(record)
        })
        writer.close()
        val alterTable = "ALTER TABLE flight ADD IF NOT EXISTS PARTITION (p_carrier='" + kv._1 + "')"
        println("+++ " + alterTable)
        stmt.execute(alterTable)
        val computeStats = "ANALYZE TABLE flight PARTITION(p_carrier='" + kv._1 + "') COMPUTE STATISTICS"
        println("+++ " + computeStats)
        stmt.execute(computeStats)
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

  def initFileSystem( hdfsNameNode: String, hadoopUser: String ): Configuration = {
    val hdfsUri = "hdfs://" + hdfsNameNode + ":9000"
    val conf: Configuration = new Configuration
    // Set FileSystem URI
    conf.set("fs.defaultFS", hdfsUri)
    // Because of Maven
    // "Nothing"
    conf.set("fs.hdfs.impl", "org.apache.hadoop.hdfs.DistributedFileSystem")
    conf.set("fs.file.impl", "org.apache.hadoop.fs.LocalFileSystem")
    // Set HADOOP user
    System.setProperty("HADOOP_USER_NAME", hadoopUser)
    System.setProperty("hadoop.home.dir", "/")
    conf
  }
}
