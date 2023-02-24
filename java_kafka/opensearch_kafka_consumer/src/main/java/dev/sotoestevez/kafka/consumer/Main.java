package dev.sotoestevez.kafka.consumer;

import dev.sotoestevez.search.OSSearchClient;
import dev.sotoestevez.wikimedia.RecentChange;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;


public class Main {

    private static final String INDEX_NAME = "wikimedia";
    private static final String GROUP_ID = "opensearch_demo";
    private static final List<String> TOPICS = Collections.singletonList("wikimedia.recentchange");

    public static void main(String[] args) throws IOException {
        var client = OSSearchClient.newInstance();
        var consumer = new ConsumerBuilder().setGroupId(GROUP_ID).withOffsetLatest().build();

        try (client; consumer) {
            client.createIndex(INDEX_NAME);
            consumer.subscribe(TOPICS);

            while (true) {
                var records = consumer.poll(Duration.ofMillis(3000));
                for (var record: records) {
                    client.insertDocument(INDEX_NAME, record.value());
                }
            }
        }
    }
}
