package dev.sotoestevez.events.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public class ConsumerBuilder {

    private static final String BOOTSTRAP_SERVERS = "127.0.0.1:29092";

    private final Properties props;

    public ConsumerBuilder() {
        this.props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    }

    @NotNull
    public ConsumerBuilder setGroupId(String groupId) {
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        return this;
    }

    @NotNull
    public ConsumerBuilder withOffsetEarliest() {
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return this;
    }

    @NotNull
    public ConsumerBuilder withOffsetLatest() {
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        return this;
    }

    @NotNull
    public ConsumerBuilder withAutoCommit(boolean value) {
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, String.valueOf(value));
        return this;
    }

    @NotNull
    public KafkaConsumer<String, String> build() {
        return new KafkaConsumer<>(props);
    }

}
