package dev.sotoestevez.events;

import dev.sotoestevez.events.consumer.ConsumerBuilder;
import dev.sotoestevez.events.poller.BatchPoll;
import dev.sotoestevez.indices.Indices;
import dev.sotoestevez.search.OSSearchClient;
import org.apache.kafka.common.errors.WakeupException;
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
    private static final Duration POLLING_TIME = Duration.ofMillis(3000);
    private static final int MINIMUM_BATCH_SIZE = 10;

    public static void main(String[] args) throws IOException {
        var client = OSSearchClient.newInstance();
        var consumer = new ConsumerBuilder().setGroupId(GROUP_ID).withOffsetLatest().withAutoCommit(false).build();

        try (client; consumer) {
            client.createIndex(Indices.WIKIMEDIA.name());
            consumer.subscribe(TOPICS);

            /* Single document indexing
            var singlePoll = new SinglePoll<>(consumer, value -> {
                try {
                    client.insertDocument(Indices.WIKIMEDIA, value);
                } catch (IOException ioe) {
                    log.error("Error indexing consumer record", ioe);
                }
            }, POLLING_TIME);
             */

            var batchPoll = new BatchPoll<>(consumer, batch -> {
                try {
                    client.bulkInsertDocument(Indices.WIKIMEDIA, batch);
                    return true;
                } catch (IOException ioe) {
                    log.error("Error bulk indexing batch", ioe);
                    return false;
                }
            }, POLLING_TIME, MINIMUM_BATCH_SIZE);

            while (true) {
                batchPoll.poll();
                log.info("Batch processed and committed");

                //singlePoll.poll();
            }
        } catch (WakeupException wup) {
            log.info("Consumer starting to shut down");
        } catch (Exception e) {
            log.error("Unexpected exception has occurred", e);
        } finally {
            consumer.close();
            client.close();
            log.info("The consumer and search client have been shut down gracefully");
        }
    }
}
