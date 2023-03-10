package dev.sotoestevez.events.producer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class ProducerFactory {

    private static final String BOOTSTRAP_SERVERS = "127.0.0.1:29092";

    @NotNull
    public static KafkaProducer<String, String> simpleProducer() {
        var props = getBaseProperties();
        return new KafkaProducer<>(props);
    }

    @NotNull
    public static KafkaProducer<String, String> highThroughputProducer() {
        var props = getBaseProperties();
        props.setProperty(ProducerConfig.LINGER_MS_CONFIG, "20");
        props.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, String.valueOf(32 * 1024));
        props.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        return new KafkaProducer<>(props);
    }

    @NotNull
    private static Properties getBaseProperties() {
        var props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.setProperty("min.insync.replicas", "2");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        return props;
    }

}
