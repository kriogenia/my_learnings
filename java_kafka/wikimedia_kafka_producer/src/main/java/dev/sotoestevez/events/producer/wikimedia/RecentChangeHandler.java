package dev.sotoestevez.events.producer.wikimedia;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.MessageEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecentChangeHandler implements EventHandler {

    private static final Logger log = LoggerFactory.getLogger(RecentChangeHandler.class.getSimpleName());

    private final KafkaProducer<String, String> producer;
    private final String topic;

    public RecentChangeHandler(KafkaProducer<String, String> producer, String topic) {
        this.producer = producer;
        this.topic = topic;
    }

    @Override
    public void onOpen() {
        // no requirement on open
    }

    @Override
    public void onClosed() {
        this.producer.close();
    }

    @Override
    public void onMessage(String event, MessageEvent messageEvent) {
        log.info(messageEvent.getData());
        producer.send(new ProducerRecord<>(topic, messageEvent.getData()));
    }

    @Override
    public void onComment(String comment) {
        // won't be monitored
    }

    @Override
    public void onError(Throwable t) {
        log.error("Error reading Stream", t);
    }
}