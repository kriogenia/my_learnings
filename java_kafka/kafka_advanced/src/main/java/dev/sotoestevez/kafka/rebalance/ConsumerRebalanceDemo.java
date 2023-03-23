package dev.sotoestevez.kafka.rebalance;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerRebalanceDemo {

    private static final Logger log = LoggerFactory.getLogger(ConsumerRebalanceDemo.class);

    private static final String GROUP_ID = "java_kafka_rebalance_consumer";
    private static final String TOPIC = "java_kafka";

    public static void main(String[] args) {
        log.info("Consumer Rebalance Demo");

        var props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // autocommit disabled to manually manage it
        props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");

        var consumer = new KafkaConsumer<String, String>(props);
        var listener = new MyConsumerRebalanceListener(consumer);

        final Thread mainThread = Thread.currentThread();

        // shutdown hook to gracefully close the consumer
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Detected a shutdown. Exiting consumer gracefully");
            consumer.wakeup();

            try {
                mainThread.join();
            } catch (InterruptedException ie) {
                log.error("Unable to join main thread", ie);
            }
        }));

        try {
            // subscribe to a topic, specifying the listener
            consumer.subscribe(Collections.singletonList(TOPIC), listener);

            while (true) {
                var records = consumer.poll(Duration.ofMillis(1000));
                for (var record: records) {
                    log.info("KEY: " + record.key() + ", VALUE: " + record.value());
                    log.info("PARTITION: " + record.partition() + ", OFFSET: " + record.offset());

                    // keep track the offset
                    listener.addOffsetToTrack(record.topic(), record.partition(), record.offset());
                }
                // commit async as all data has been consumed and there's no intention to block the thread
                consumer.commitAsync();
            }
        } catch (WakeupException wue) {
            log.info("Consumer is starting to shutdown");
        } catch (Exception e) {
            log.error("Unexpected exception in the consumer", e);
        } finally {
            try {
                // commit the offset before closing the consumer
                consumer.commitSync(listener.getCurrentOffsets());
            } finally {
                consumer.close();
                log.info("The consumer has been gracefully closed and the offsets committed");
            }
        }

    }
}