package dev.sotoestevez.kafka.stream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class.getSimpleName());

    private static final Properties properties;
    private static final String INPUT_TOPIC = "wikimedia.recentchange";

    static {
        properties = new Properties();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, "wikimedia-stats-application");
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
    }

    public static void main(String[] args) {
        var builder = new StreamsBuilder();
        var changeJsonStream = builder.stream(INPUT_TOPIC);

        // processors

        final Topology topology = builder.build();
        log.info("Topology: {}", topology.describe());

        var streams = new KafkaStreams(topology, properties);
        streams.start();
    }
}