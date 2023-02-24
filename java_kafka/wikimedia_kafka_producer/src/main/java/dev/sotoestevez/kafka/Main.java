package dev.sotoestevez.kafka;

import dev.sotoestevez.kafka.wikimedia.RecentChangeProducer;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        var producer = ProducerFactory.highThroughputProducer();
        var wikimediaProducer = new RecentChangeProducer(producer);
        wikimediaProducer.run();
        TimeUnit.MINUTES.sleep(1);
    }
}
