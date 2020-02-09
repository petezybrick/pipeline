package com.petezybrick.pipeline.spark.basic

import java.io.{File, IOException}
import java.util.UUID

import org.apache.avro.Schema
import org.apache.avro.generic.GenericData
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.avro.AvroParquetWriter
import org.apache.parquet.hadoop.ParquetWriter
import org.apache.parquet.hadoop.metadata.CompressionCodecName


object TryTip {
  def main(args: Array[String]): Unit = {
    val tip1: Tip = new Tip(business_id = "b01", date = "2001-01-01 11:11:11", likes = 100, text = "aaa", `type` = "t01", user_id = "u01")
    val tip2: Tip = new Tip(business_id = "b02", date = "2002-02-02 22:22:22", likes = 200, text = "bbb", `type` = "t02", user_id = "u02")

    val tipSerde: TipSerde = new TipSerde
    val bytes1: Array[Byte] = tipSerde.serialize(tip1)
    val tip1De: Tip = tipSerde.deserialize(bytes1)
    println(tip1De)


    import scala.collection.mutable.ArrayBuffer
    val ab1 = ArrayBuffer[Int](1, 2, 3)
    ab1 += 98
    ab1 += 99
    ab1.foreach(println(_))

    val seq1: Seq[Int] = ab1.toSeq

    var abBytes: ArrayBuffer[Array[Byte]] = ArrayBuffer[Array[Byte]]()
    for (i <- 0 until 10) {
      val tip: Tip = new Tip(business_id = "b" + i, date = "2001-01-01 11:11:11", likes = i, text = "aaa", `type` = "t" + i, user_id = "u" + i)
      abBytes += tipSerde.serialize(tip)
    }

    println("++++++++")
    abBytes.foreach(abByte => println(tipSerde.deserialize(abByte)))

    // org.codehaus.jackson.JsonParseException
    val xx: org.codehaus.jackson.JsonParseException = null
    val tempFileNameExt = UUID.randomUUID.toString + ".avro"
    //val tempPathName = System.getProperty("java.io.tmpdir") + tempFileNameExt
    val tempPathName = "pipeline-spark/docker/shared/" + tempFileNameExt

    println(tempPathName)
    try {
      new File(tempPathName).delete()
      val writer: ParquetWriter[GenericData.Record] = createWriter(tempPathName, tipSerde.schema)
      for (i <- 0 until 10) {
        val tip: Tip = new Tip(business_id = "b" + i, date = "2001-01-01 11:11:11", likes = i, text = "aaa", `type` = "t" + i, user_id = "u" + i)
        writer.write(tipSerde.toGenericRecord(tip))
      }
      writer.close()
    } catch {
      case ex: Exception => {
        println(ex)
        throw new RuntimeException(ex)
      }
    }
    // TODO: Read file based on schema, run some queries

  }

  @throws[IOException]
  def createWriter(pathName: String, schemaTo: Schema): ParquetWriter[GenericData.Record] = {
    var writer = AvroParquetWriter.builder[GenericData.Record](new Path(pathName)).withSchema(schemaTo).withConf(new Configuration).withCompressionCodec(CompressionCodecName.SNAPPY).build
    writer
  }

}
