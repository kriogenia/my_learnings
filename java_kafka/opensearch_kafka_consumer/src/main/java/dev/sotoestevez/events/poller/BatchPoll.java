package dev.sotoestevez.events.poller;

import dev.sotoestevez.events.consumer.ConsumerHelper;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BatchPoll<K, V> {

    private static final Logger log = LoggerFactory.getLogger(BatchPoll.class.getSimpleName());

    private final KafkaConsumer<K, V> consumer;
    private final Predicate<Collection<V>> action;
    private final Duration duration;
    private final int minSize;
    private final List<V> currentBatch;

    public BatchPoll(KafkaConsumer<K, V> consumer, Predicate<Collection<V>> action, Duration duration, int minSize) {
        this.consumer = consumer;
        this.action = action;
        this.duration = duration;
        this.minSize = minSize;
        this.currentBatch = new ArrayList<>();
    }

    public void poll() {
        var records = consumer.poll(duration);
        currentBatch.addAll(ConsumerHelper.getValues(records));
        log.info("Received new batch with {} records. Total queued: {}", records.count(), currentBatch.size());

        if (currentBatch.size() < minSize) {
            return;
        }

        if (action.test(currentBatch)) {
            currentBatch.clear();
            consumer.commitAsync();
        }
    }

}
