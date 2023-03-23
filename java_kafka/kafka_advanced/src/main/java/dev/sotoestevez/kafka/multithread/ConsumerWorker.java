package dev.sotoestevez.kafka.multithread;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerWorker implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ConsumerWorker.class);

    private static final String GROUP_ID = "multithread_java_kafka_consumer";
    private static final String TOPIC = "java_kafka";

    private final String id;

    private CountDownLatch countDownLatch;
    private Consumer<String, String> consumer;

    public ConsumerWorker(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    private void init() {
        countDownLatch = new CountDownLatch(1);

        // create properties
        final var props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC));

        log.info("Worker " + id + " initiated");
    }

    @Override
    public void run() {
        init();

        try {
            while (true) {
                final var records = consumer.poll(Duration.ofMillis(100));
                for (var record: records) {
                    log.info("WORKER: " + id + " -> KEY: " + record.key() + ", VALUE: " + record.value());
                }
            }
        } catch (WakeupException wue) {
            log.warn("Consumer poll woke up");
        } finally {
            consumer.close();
            countDownLatch.countDown();
        }
    }

    public void shutdown() throws InterruptedException {
        consumer.wakeup();
        countDownLatch.await();
        log.info("Consumer closed");
    }

    public static class ConsumerCloser implements Runnable {

        private static final Logger log = LoggerFactory.getLogger(ConsumerCloser.class);

        private final ConsumerWorker worker;

        public ConsumerCloser(final ConsumerWorker worker) {
            this.worker = worker;
        }

        @Override
        public void run() {
            try {
                worker.shutdown();
            } catch (InterruptedException e) {
                log.error("Error shutting down consumer", e);
            }
        }
    }

}
