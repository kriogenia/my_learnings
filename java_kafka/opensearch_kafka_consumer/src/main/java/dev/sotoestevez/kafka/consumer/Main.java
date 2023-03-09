package dev.sotoestevez.kafka.consumer;

import dev.sotoestevez.indices.Indices;
import dev.sotoestevez.search.OSSearchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class.getSimpleName());

    private static final String GROUP_ID = "opensearch_demo";
    private static final List<String> TOPICS = Collections.singletonList("wikimedia.recentchange");

    public static void main(String[] args) throws IOException {
        var client = OSSearchClient.newInstance();
        var consumer = new ConsumerBuilder().setGroupId(GROUP_ID).withOffsetLatest().build();

        try (client; consumer) {
            client.createIndex(Indices.WIKIMEDIA.name());
            consumer.subscribe(TOPICS);

            while (true) {
                var records = consumer.poll(Duration.ofMillis(3000));
                for (var record: records) {
                    try {
                        client.insertDocument(Indices.WIKIMEDIA, record.value());
                    } catch (IOException ioe) {
                        log.error("Error indexing consumer record", ioe);
                    }
                }
            }
        }
    }
}
