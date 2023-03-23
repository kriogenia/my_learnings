package dev.sotoestevez.kafka.rebalance;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MyConsumerRebalanceListener implements ConsumerRebalanceListener {

    private static final Logger log = LoggerFactory.getLogger(MyConsumerRebalanceListener.class);

    private final KafkaConsumer<String, String> consumer;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public MyConsumerRebalanceListener(KafkaConsumer<String, String> consumer) {
        this.consumer = consumer;
    }

    public void addOffsetToTrack(String topic, int partition, long offset) {
        currentOffsets.put(
                new TopicPartition(topic, partition),
                new OffsetAndMetadata(offset + 1)
        );
    }

    public Map<TopicPartition, OffsetAndMetadata> getCurrentOffsets() {
        return currentOffsets;
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        log.info("Partitions revoked. Committing offsets: {}", currentOffsets);
        consumer.commitSync(currentOffsets);
    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        log.info("Partitions assigned");
    }

}
