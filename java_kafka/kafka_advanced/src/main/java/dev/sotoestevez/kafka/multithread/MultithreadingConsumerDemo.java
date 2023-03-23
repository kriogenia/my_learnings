package dev.sotoestevez.kafka.multithread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

public class MultithreadingConsumerDemo {

    private static final Logger log = LoggerFactory.getLogger(MultithreadingConsumerDemo.class);

    private static final int NUM_WORKERS = 2;

    public static void main(String[] args) {
        log.info("Multithreading consumer demo");

        var workers = IntStream.range(0, NUM_WORKERS).mapToObj(id -> new ConsumerWorker(String.valueOf(id))).toList();

        for (var worker: workers) {
            new Thread(worker).start();
            log.info("Launched thread for worker {}", worker.getId());
            Runtime.getRuntime().addShutdownHook(new Thread(new ConsumerWorker.ConsumerCloser(worker)));
        }
    }

}
