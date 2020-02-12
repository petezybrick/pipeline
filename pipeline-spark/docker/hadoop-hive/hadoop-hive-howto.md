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

CREATE TABLE tip_temp
STORED AS AVRO
TBLPROPERTIES ('avro.schema.url'='hdfs://pipeline-hive-namenode:9000/user/pipeline/avro_schema/tip.avsc')

CREATE EXTERNAL TABLE tip
LIKE tip_temp
STORED AS PARQUET
LOCATION 'hdfs://pipeline-hive-namenode:9000/user/pipeline/db_demo/tip'

drop table tip_temp

==========================================

CREATE TABLE flight_temp
PARTITIONED BY (p_carrier string)
STORED AS AVRO
TBLPROPERTIES ("avro.schema.url"="hdfs://pipeline-hive-namenode:9000/user/pipeline/avro_schema/flight.avsc")

CREATE EXTERNAL TABLE flight
LIKE flight_temp
STORED AS PARQUET
LOCATION 'hdfs://pipeline-hive-namenode:9000/user/pipeline/db_pipeline/flight/'

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
+ manually create p_carrier folder and manually put files into that folder
+ run query, verify
+ programmatic hdfs put, rm, mkdir


CREATE EXTERNAL TABLE flight_pz ( flight_id string, dofw integer)
STORED AS PARQUET
LOCATION 'hdfs://pipeline-hive-namenode:9000/user/pipeline/db_pipeline/flight/'

