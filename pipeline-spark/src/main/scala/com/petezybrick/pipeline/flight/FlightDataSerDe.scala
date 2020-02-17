package com.petezybrick.pipeline.flight

import java.io.ByteArrayOutputStream

import org.apache.avro.Schema
import org.apache.avro.generic.{GenericData, GenericDatumReader, GenericDatumWriter, GenericRecord}
import org.apache.avro.io.{DecoderFactory, EncoderFactory}

import scala.io.Source

/**
  * Created by alvinjin on 2017-03-14.
  */


class FlightDataSerDe extends Serializable {

  val avroSchema = Source.fromInputStream(getClass.getResourceAsStream("/schema/flight.avsc")).mkString
  val schema = new Schema.Parser().parse(avroSchema)

//  val reader = new GenericDatumReader[GenericRecord](schema)
//  val writer = new GenericDatumWriter[GenericRecord](schema)

  def serialize(flight: Flight): Array[Byte] = {
    val out = new ByteArrayOutputStream()
    val encoder = EncoderFactory.get.binaryEncoder(out, null)
    val avroRecord = toGenericRecord(flight)
    val writer = new GenericDatumWriter[GenericRecord](schema)
    writer.write(avroRecord, encoder)
    encoder.flush
    out.close
    out.toByteArray

  }

  def toGenericRecord(flight: Flight): GenericData.Record =  {

    val avroRecord = new GenericData.Record(schema)
    avroRecord.put("flight_id", flight._id)
    avroRecord.put("dofW", flight.dofW)
    avroRecord.put("carrier", flight.carrier)
    avroRecord.put("origin", flight.origin)
    avroRecord.put("dest", flight.dest)
    avroRecord.put("crsdephour", flight.crsdephour)
    avroRecord.put("crsdeptime", flight.crsdeptime)
    avroRecord.put("depdelay", flight.depdelay)
    avroRecord.put("crsarrtime", flight.crsarrtime)
    avroRecord.put("arrdelay", flight.arrdelay)
    avroRecord.put("crselapsedtime", flight.crselapsedtime)
    avroRecord.put("dist", flight.dist)
    avroRecord
  }


  def deserialize(bytes: Array[Byte]): Flight = {
    val decoder = DecoderFactory.get.binaryDecoder(bytes, null)
    val reader = new GenericDatumReader[GenericRecord](schema)
    val record = reader.read(null, decoder)
    Flight(
      record.get("flight_id").toString,
      record.get("dofW").asInstanceOf[Int],
      record.get("carrier").toString,
      record.get("origin").toString,
      record.get("dest").toString,
      record.get("crsdephour").asInstanceOf[Int],
      record.get("crsdeptime").asInstanceOf[Double],
      record.get("depdelay").asInstanceOf[Double],
      record.get("crsarrtime").asInstanceOf[Double],
      record.get("arrdelay").asInstanceOf[Double],
      record.get("crselapsedtime").asInstanceOf[Double],
      record.get("dist").asInstanceOf[Double]
    )
  }
}
