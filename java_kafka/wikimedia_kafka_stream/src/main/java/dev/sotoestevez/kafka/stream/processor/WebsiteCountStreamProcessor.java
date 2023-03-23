package dev.sotoestevez.kafka.stream.processor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

public class WebsiteCountStreamProcessor implements StreamProcessor<String, String> {

    private static final Logger log = LoggerFactory.getLogger(WebsiteCountStreamProcessor.class.getSimpleName());

    private static final String WEBSITE_COUNT_STORE = "website-count-store";
    private static final String WEBSITE_COUNT_TOPIC = "wikimedia.stats.website";

    private static final String WEBSITE_PROPERTY = "website";
    private static final String COUNT_PROPERTY = "count";

    private final ObjectMapper mapper;
    private final Duration windowDuration;

    public WebsiteCountStreamProcessor(ObjectMapper mapper, Duration windowDuration) {
        this.mapper = mapper;
        this.windowDuration = windowDuration;
    }

    @Override
    public void setUp(KStream<String, String> stream) {
        final var timeWindows = TimeWindows.ofSizeWithNoGrace(windowDuration);
        stream.selectKey((k, v) -> {
                    try {
                        return mapper.readValue(v, WebsiteInfo.class).serverName;
                    } catch (JsonProcessingException e) {
                        log.warn("Error deserializing website info: {}", e.getMessage());
                        return "server_name_parse_error";
                    }
                })
                .groupByKey()
                .windowedBy(timeWindows)
                .count(Materialized.as(WEBSITE_COUNT_STORE))
                .toStream()
                .mapValues((k, v) -> {
                    final var kvMap = Map.of(
                            WEBSITE_PROPERTY, k.key(),
                            COUNT_PROPERTY, v
                    );
                    try {
                        return mapper.writeValueAsString(kvMap);
                    } catch (JsonProcessingException e) {
                        log.error("Error serializing website count info", e);
                        return null;
                    }
                })
                .to(WEBSITE_COUNT_TOPIC, Produced.with(
                        WindowedSerdes.timeWindowedSerdeFrom(String.class, timeWindows.size()),
                        Serdes.String()
                ));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record WebsiteInfo(
            @JsonProperty(value = "server_name") String serverName
    ) {
    }


}
