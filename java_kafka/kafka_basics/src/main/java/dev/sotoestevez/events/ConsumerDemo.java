package dev.sotoestevez.events;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerDemo {

    private static final Logger log = LoggerFactory.getLogger(ConsumerDemo.class.getSimpleName());

    private static final String GROUP_ID = "java_kafka_consumer";

    public static void main(String[] args) {
        log.info("Hello consumer!");

        // create properties
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "127.0.0.1:9092");
        props.setProperty("key.deserializer", StringDeserializer.class.getName());
        props.setProperty("value.deserializer", StringDeserializer.class.getName());
        props.setProperty("group.id", GROUP_ID);
        props.setProperty("auto.offset.reset", "earliest");

        // create a consumer
        var consumer = new KafkaConsumer<String, String>(props);

        // get a reference to the main thread
        final var mainThread = Thread.currentThread();
        // and a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown detected, exiting gracefully...");
            consumer.wakeup();

            // join the main thread to execute the code
            try {
                mainThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }));

        try {
            // subscribe to a topic
            consumer.subscribe(Collections.singletonList(ProducerDemo.TOPIC));

            // poll for data
            while (true) {
                log.info("Polling");
                var records = consumer.poll(Duration.ofMillis(1000));
                for (var record: records) {
                    log.info("KEY: " + record.key() + ", VALUE: " + record.value());
                    log.info("PARTITION: " + record.partition() + ", OFFSET: " + record.offset());
                }
            }
        } catch (WakeupException wue) {
            log.info("Consumer is starting to shutdown");
        } catch (Exception e) {
            log.error("Unexpected exception in the consumer", e);
        } finally {
            consumer.close();
            log.info("Consumer closed");
        }
    }
}
