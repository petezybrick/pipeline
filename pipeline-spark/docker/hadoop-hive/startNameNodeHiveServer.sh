#!/bin/bash
hdfs namenode &
sleep 5
hiveserver2 &
sleep 5
hive --service metastore
