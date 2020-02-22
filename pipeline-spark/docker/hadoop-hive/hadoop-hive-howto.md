create the image hadoop hive image
cd hadoop-hive
docker build -t hadoop-hive:2.9.0 .

first time, mysql needs hive db setup

docker exec -it pipeline-hive-mysql /bin/bash
cd /tmp/shared/
mysql -p
    enter Password*8
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' IDENTIFIED BY 'Password*8';
FLUSH PRIVILEGES;
drop database if exists metastore;
create database metastore;
use metastore;
source hive-schema-2.3.0.mysql.sql;

docker exec -it pipeline-hive-namenode /bin/bash
hdfs dfs -mkdir /data
hdfs dfs -mkdir /data/flight
hdfs dfs -mkdir /user
hdfs dfs -mkdir /user/pipeline
hdfs dfs -mkdir /user/pipeline/db_pipeline
hdfs dfs -mkdir /user/pipeline/avro_schema
hdfs dfs -put /tmp/shared/flight.avsc /user/pipeline/avro_schema/flight.avsc
hdfs dfs -put /tmp/shared/flights20170102.json /data/flight/flights20170102.json

CREATE DATABASE db_pipeline
CREATE TABLE db_pipeline.flight_temp
PARTITIONED BY (p_carrier string)
STORED AS AVRO
TBLPROPERTIES ("avro.schema.url"="hdfs://pipeline-hive-namenode:9000/user/pipeline/avro_schema/flight.avsc");

CREATE EXTERNAL TABLE db_pipeline.flight
LIKE db_pipeline.flight_temp
STORED AS PARQUET
LOCATION '/user/pipeline/db_pipeline/flight';

build the pipeline-spark jar, submit, verify success
./bin/spark-submit \
 --class com.petezybrick.pipeline.flight.FlightDataRun \
 --deploy-mode cluster \
 --master spark://pipeline-spark-master:7077 \
 --executor-memory 8G \
 --executor-cores 2 \
 --total-executor-cores 6 \
 /tmp/shared/pipeline-spark-1.0.0.jar
 
play with spark sql directly accessing the flight table in parquet
docker exec -it pipeline-spark-master /bin/bash
./bin/spark-shell
val flightDF = spark.read.parquet("hdfs://pipeline-hive-namenode:9000/user/pipeline/db_pipeline/flight")
flightDF.createOrReplaceTempView("flight")
val flightDestsDF = spark.sql("SELECT dest FROM flight WHERE p_carrier='AA'")
flightDestsDF.map( attributes => "Dest: " + attributes(0)).show


These are now done programmatically
ALTER TABLE flight ADD IF NOT EXISTS PARTITION (p_carrier='DL')
ALTER TABLE flight ADD IF NOT EXISTS PARTITION (p_carrier='AA')
ALTER TABLE flight ADD IF NOT EXISTS PARTITION (p_carrier='UA')
ALTER TABLE flight ADD IF NOT EXISTS PARTITION (p_carrier='WN')

ANALYZE TABLE flight PARTITION(p_carrier='DL') COMPUTE STATISTICS;
ANALYZE TABLE flight PARTITION(p_carrier='AA') COMPUTE STATISTICS;
ANALYZE TABLE flight PARTITION(p_carrier='UA') COMPUTE STATISTICS;
ANALYZE TABLE flight PARTITION(p_carrier='WN') COMPUTE STATISTICS;


/user/pipeline/db_pipeline/flight/p_carrier=AA

Next
+ programmatic spark sql
+ jupyter in docker compose
+ solr in docker compose - after ML exercises




==========================================


CREATE TABLE tip_temp
STORED AS AVRO
TBLPROPERTIES ('avro.schema.url'='hdfs://pipeline-hive-namenode:9000/user/pipeline/avro_schema/tip.avsc')

CREATE EXTERNAL TABLE tip
LIKE tip_temp
STORED AS PARQUET
LOCATION 'hdfs://pipeline-hive-namenode:9000/user/pipeline/db_demo/tip'

drop table tip_temp

