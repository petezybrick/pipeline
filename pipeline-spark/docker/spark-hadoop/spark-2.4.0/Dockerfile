FROM ubuntu-java-python-nano:18.04
MAINTAINER Pete Zybrick <pzybrick@mdsol.com>

RUN apt-get update \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

copy spark-2.4.0-bin-hadoop2.7.tgz spark-2.4.0-bin-hadoop2.7.tgz

# SPARK
ENV SPARK_VERSION 2.4.0
ENV SPARK_PACKAGE spark-${SPARK_VERSION}-bin-hadoop2.7
ENV SPARK_HOME /usr/spark-${SPARK_VERSION}
ENV PATH $PATH:${SPARK_HOME}/bin
RUN tar -xzvf ${SPARK_PACKAGE}.tgz -C /usr/ 

RUN mv /usr/$SPARK_PACKAGE $SPARK_HOME \
   && chown -R root:root $SPARK_HOME

WORKDIR $SPARK_HOME
CMD ["bin/spark-class", "org.apache.spark.deploy.master.Master"]