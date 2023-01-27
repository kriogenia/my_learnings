package dev.sotoestevez.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemo {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemo.class.getSimpleName());

    public static final String TOPIC = "java_kafka";

    public static void main(String[] args) {
        log.info("Hello producer!");

        // create properties
        var props = new Properties();
        props.setProperty("bootstrap.servers", "127.0.0.1:9092");
        props.setProperty("key.serializer", StringSerializer.class.getName());
        props.setProperty("value.serializer", StringSerializer.class.getName());

        log.info(props.toString());

        // create producer
        var producer = new KafkaProducer<String, String>(props);

        // send data
        for (int i = 0; i < 10; i++) {
            var key = "id_" + i;
            var value = String.valueOf(i);

            var record = new ProducerRecord<>(TOPIC, key,  value);
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