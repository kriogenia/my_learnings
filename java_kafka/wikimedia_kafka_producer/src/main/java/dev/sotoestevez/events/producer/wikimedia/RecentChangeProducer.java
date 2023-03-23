package dev.sotoestevez.events.producer.wikimedia;

import com.launchdarkly.eventsource.EventSource;
import dev.sotoestevez.events.producer.ProducerFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class RecentChangeProducer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(RecentChangeProducer.class.getSimpleName());

    private static final URI WIKIMEDIA_URL = URI.create("https://stream.wikimedia.org/v2/stream/recentchange");
    public static final String TOPIC = "wikimedia.recentchange";

    private final KafkaProducer<String, String> producer;

    public RecentChangeProducer(KafkaProducer<String, String> producer) {
        this.producer = producer;
    }

    @Override
    public void run() {
        log.info("Starting Wikimedia producer!");

        //var producer = ProducerFactory.simpleProducer();
        var producer = ProducerFactory.highThroughputProducer();
        var eventHandler = new dev.sotoestevez.events.producer.wikimedia.RecentChangeHandler(producer, TOPIC);
        var eventSource = new EventSource.Builder(eventHandler, WIKIMEDIA_URL).build();

        eventSource.start();
    }
}
