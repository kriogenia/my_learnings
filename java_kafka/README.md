# Java Kafka

This Java project holds a series of modules and little projects developed during my learnings of
**Apache Kafka**. On this project you can find the following files and modules:

## Kafka Basics

At [kafka_basics](./kafka_basics). It contains two Main files with one demo each. One of those
creates a Kafka **producer** to populate a topic and the other creates a **consumer** to read that
same topic. To run it just execute the different demo files.

## Kafka Advanced

At [kafka_advanced](./kafka_advanced). It contains three demos with more complex consumers: a
consumer wrapped in a worker to be run into a _separate thread_, a consumer with _rebalancing_
capabilities and a consumer using the _seek_ and _assign_ APIs. To run it just execute the 
different demo files.

## Wikimedia to OpenSearch

Project closer to real enterprise projects. It polls data from Wikimedia's [recentchange](https://www.mediawiki.org/wiki/API:RecentChanges),
extracts some statistics from
the data and finally index both the data and the stats into an OpenSearch instance. It is
composed of the following modules and components:

* The [Docker compose](./docker-compose.yml) file to launch a Kafka cluster with two Zookeepers, 
two Kafka brokers and one Kafka schema registry. Running this file deploys the environment needed
for the Kafka components of this project to work.
* The [Wikimedia Kafka Producer](./wikimedia_kafka_producer) is the producer that will read the
data from Wikimedia and send it to the topic.
* The [Wikimedia Kafka Stream](./wikimedia_kafka_stream) is a Kafka Stream to read from the topic
of the recentchanges and compute some statistics out it. It counts the number of articles edited by
bots, it... and it... Those stats are finally forwarded to different topics where they can be
indexed into OpenSearch using the [ElasticSearch Service Sink Connector](https://docs.confluent.io/kafka-connectors/elasticsearch/current/overview.html).
* The [OpenSearch Kafka Consumer](./opensearch_kafka_consumer) is the last component. It's a consumer
to ready to read from topics and index them into OpenSearch. In this project it works to index the data
read from the recentchanges, but it's not coupled with it. Inside this project is another [Docker compose file](./opensearch_kafka_consumer/docker-compose.yml)
to handle the launch of the OpenSearch instance.

The way to run this project is to launch both compose files and then execute the modules. Take into account
that there's no consumer to index the stats data so the use of a Kafka Sink to do it.