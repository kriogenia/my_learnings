package dev.sotoestevez.kafka.stream.builder;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sotoestevez.kafka.stream.processor.BotCountStreamProcessor;
import dev.sotoestevez.kafka.stream.processor.StreamProcessor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class WikimediaStatsStreamBuilder {

    private static final Logger log = LoggerFactory.getLogger(WikimediaStatsStreamBuilder.class.getSimpleName());

    public static final String BOOTSTRAP_SERVERS = "127.0.0.1:29092";

    private final Properties properties;
    private final StreamsBuilder builder;
    private final KStream<String, String> changeJsonStream;

    public static final List<StreamProcessor<String, String>> DEFAULT_STATS_PROCESSORS;
    static {
        DEFAULT_STATS_PROCESSORS = new ArrayList<>();
        var mapper = new ObjectMapper();
        DEFAULT_STATS_PROCESSORS.add(new BotCountStreamProcessor(mapper));
    }

    public WikimediaStatsStreamBuilder(String appId, String topic) {
        this.properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, appId);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        this.builder = new StreamsBuilder();
        this.changeJsonStream = builder.stream(topic);
    }

    public WikimediaStatsStreamBuilder withProcessor(StreamProcessor<String, String> processor) {
        processor.setUp(changeJsonStream);
        return this;
    }

    public KafkaStreams build() {
        final var topology = builder.build();
        log.info("Topology: {}", topology.describe());
        return new KafkaStreams(topology, this.properties);
    }

}
