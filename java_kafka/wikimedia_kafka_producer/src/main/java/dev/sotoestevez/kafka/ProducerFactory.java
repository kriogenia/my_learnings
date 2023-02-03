package dev.sotoestevez.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class ProducerFactory {

    private static final String BOOTSTRAP_SERVERS = "127.0.0.1:29092";

    public static KafkaProducer<String, String> newProducer() {
        var props = new Properties();
        props.setProperty("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.setProperty("key.serializer", StringSerializer.class.getName());
        props.setProperty("value.serializer", StringSerializer.class.getName());
        return new KafkaProducer<>(props);
    }

}
