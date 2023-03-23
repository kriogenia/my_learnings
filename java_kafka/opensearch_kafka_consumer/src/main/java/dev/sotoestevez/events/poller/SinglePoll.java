package dev.sotoestevez.events.poller;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.function.Consumer;

public class SinglePoll<K, V> {

    private static final Logger log = LoggerFactory.getLogger(SinglePoll.class.getSimpleName());

    private final KafkaConsumer<K, V> consumer;
    private final Consumer<V> action;
    private final Duration duration;

    public SinglePoll(KafkaConsumer<K, V> consumer, Consumer<V> action, Duration duration) {
        this.consumer = consumer;
        this.action = action;
        this.duration = duration;
    }

    public void poll() {
        var records = consumer.poll(duration);
        for (var record: records) {
            action.accept(record.value());
        }
        consumer.commitAsync();
    }

}
