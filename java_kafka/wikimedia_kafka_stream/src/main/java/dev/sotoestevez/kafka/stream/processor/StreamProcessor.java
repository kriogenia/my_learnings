package dev.sotoestevez.kafka.stream.processor;

import org.apache.kafka.streams.kstream.KStream;

@FunctionalInterface
public interface StreamProcessor<K, V> {

    void setUp(KStream<K, V> stream);

}
