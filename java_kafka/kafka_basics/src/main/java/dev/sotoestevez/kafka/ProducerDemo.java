package dev.sotoestevez.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemo {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemo.class.getSimpleName());

    private static final String TOPIC = "kafka_java";

    public static void main(String[] args) {
        log.info("Hello world!");

        // create properties
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "127.0.0.1:9092");
        props.setProperty("key.serializer", StringSerializer.class.getName());
        props.setProperty("value.serializer", StringSerializer.class.getName());

        log.info(props.toString());

        // create producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // send data
        for (int i = 0; i < 10; i++) {
            String key = "id_" + i;
            String value = String.valueOf(i);

            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC, key,  value);
            producer.send(record, (metadata, exception) -> {
                if (exception == null) {
                    log.info("TOPIC: " + metadata.topic());
                    log.info("KEY: " + key);
                    log.info("PARTITION: " + metadata.partition() );
                    log.info("OFFSET: " + metadata.offset());
                    log.info("TIMESTAMP: " + metadata.timestamp());
                } else {
                    log.error("Error while producing", exception );
                }
            });
        }

        // flush and close producer
        producer.flush();
        producer.close();
    }
}