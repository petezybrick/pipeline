cd to location of ubuntu dockerfile, i.e. 
	cd ~/IdeaProjects/pipeline/pipeline-spark/docker/ubuntu-java-python-nano
docker build -t ubuntu-java-python-nano:18.04 .

just to check out the structure, create a container docker run -it --rm ubuntu-java-python-nano:18.04

docker run -e CLUSTER_NAME=test -it --rm bde2020/hadoop-namenode:2.0.0-hadoop3.1.3-java8
