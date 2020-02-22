download the tgz
cd to this folder
docker build -t spark:2.4.0 .

cd <path>/spark-2.4.0-bin-hadoop2.7

add 127.0.0.1 spark-master to /etc/hosts

./bin/spark-submit \
  --class org.apache.spark.examples.SparkPi \
  --deploy-mode cluster \
  --master spark://spark-master:7077 \
  --executor-memory 8G \
  --executor-cores 2 \
  --total-executor-cores 6 \
  /usr/spark-2.4.0/examples/jars/spark-examples_2.11-2.4.0.jar \
  1000
  
Console: http://localhost:8080/

Verify successful installation
docker exec -it spark-master /bin/bash
./bin/spark-shell --master spark://spark-master:7077
:quit

./bin/spark-submit \
  --class org.apache.spark.examples.SparkPi \
  --deploy-mode cluster \
  --master spark://spark-master:7077 \
  --executor-memory 8G \
  --executor-cores 2 \
  --total-executor-cores 6 \
  /usr/spark-2.4.0/examples/jars/spark-examples_2.11-2.4.0.jar \
  1000

./bin/spark-submit \
--class com.mdsol.msgbus.consumer.template.spark.stream.scala.StreamReaderKinesisScala \
--deploy-mode cluster \
--master spark://spark-master:6066 \
--executor-memory 8G \
--executor-cores 1 \
--total-executor-cores 4 \
/tmp/shared/jars/message-bus-poc-consumer-template-spark-2019.1.0.jar sandbox_zybrick

./bin/spark-submit \
--class com.mdsol.msgbus.consumer.template.spark.stream.scala.StreamReaderKinesisScala \
--master spark://spark-master:6066 \
--executor-memory 8G \
--executor-cores 1 \
--total-executor-cores 4 \
/tmp/shared/jars/message-bus-poc-consumer-template-spark-2019.1.0.jar sandbox_zybrick

hdfs dfs -mkdir /data
hdfs dfs -mkdir /data/flight
hdfs dfs -mkdir /user
hdfs dfs -mkdir /user

hdfs dfs -put /tmp/shared/flights20170102.json /data/flight/flights20170102.json


./bin/spark-submit \
 --class com.petezybrick.pipeline.spark.basic.SparkPi \
 --deploy-mode cluster \
 --master spark://pipeline-spark-master:7077 \
 --executor-memory 8G \
 --executor-cores 2 \
 --total-executor-cores 6 \
 /tmp/shared/pipeline-spark-1.0.0.jar \
 10

./bin/spark-submit \
 --class com.petezybrick.pipeline.flight.SparkPi \
 --deploy-mode cluster \
 --master spark://pipeline-spark-master:7077 \
 --executor-memory 8G \
 --executor-cores 2 \
 --total-executor-cores 6 \
 /tmp/shared/pipeline-spark-1.0.0.jar \
 10

./bin/spark-submit \
 --class com.petezybrick.pipeline.flight.FlightDataRun \
 --deploy-mode cluster \
 --master spark://pipeline-spark-master:7077 \
 --executor-memory 8G \
 --executor-cores 2 \
 --total-executor-cores 6 \
 /tmp/shared/pipeline-spark-1.0.0.jar

 