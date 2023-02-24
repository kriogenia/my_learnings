package dev.sotoestevez.kafka.wikimedia;

import com.launchdarkly.eventsource.EventSource;
import dev.sotoestevez.kafka.ProducerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class RecentChangeProducer {

    private static final Logger log = LoggerFactory.getLogger(RecentChangeProducer.class.getSimpleName());

    private static final URI WIKIMEDIA_URL = URI.create("https://stream.wikimedia.org/v2/stream/recentchange");
    public static final String TOPIC = "wikimedia.recentchange";

    public static void main(String[] args) throws InterruptedException {
        log.info("Starting Wikimedia producer!");

        //var producer = ProducerFactory.simpleProducer();
        var producer = ProducerFactory.highThroughputProducer();
        var eventHandler = new RecentChangeHandler(producer, TOPIC);
        var eventSource = new EventSource.Builder(eventHandler, WIKIMEDIA_URL).build();

        eventSource.start();

        TimeUnit.MINUTES.sleep(1);
    }

}
