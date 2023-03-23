package dev.sotoestevez.kafka.seekandassign;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class SeekAndAssignDemo {

    private static final Logger log = LoggerFactory.getLogger(SeekAndAssignDemo.class);

    private static final String TOPIC = "java_kafka";

    private static final int PARTITION_TO_READ_FROM = 0;
    private static final long OFFSET_TO_READ_FROM = 2L;
    private static final int NUM_MESSAGES_TO_READ = 5;

    public static void main(String[] args) {
        log.info("Seek and assign API demo");

        // create properties
        var props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // create a consumer
        var consumer = new KafkaConsumer<String, String>(props);

        // assign
        var partitionToReadFrom = new TopicPartition(TOPIC, PARTITION_TO_READ_FROM);
        consumer.assign(Collections.singletonList(partitionToReadFrom));

        // seek
        consumer.seek(partitionToReadFrom, OFFSET_TO_READ_FROM);

        var messagesRead = 0;
        polling: while (true) {
            var records = consumer.poll(Duration.ofMillis(100));
            for (var record: records) {
                messagesRead++;
                log.info("KEY: " + record.key() + ", VALUE: " + record.value());
                log.info("PARTITION: " + record.partition() + ", OFFSET: " + record.offset());
                if (messagesRead >= NUM_MESSAGES_TO_READ) {
                    break polling;
                }
            }
        }
    }


}
