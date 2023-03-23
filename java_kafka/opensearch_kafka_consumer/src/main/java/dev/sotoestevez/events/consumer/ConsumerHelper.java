package dev.sotoestevez.events.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ConsumerHelper {

    public static <V> Collection<V> getValues(ConsumerRecords<?, V> records) {
        return StreamSupport.stream(records.spliterator(), false).map(ConsumerRecord::value).collect(Collectors.toList());
    }

}
