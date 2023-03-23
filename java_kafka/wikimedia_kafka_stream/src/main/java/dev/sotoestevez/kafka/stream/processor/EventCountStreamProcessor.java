package dev.sotoestevez.kafka.stream.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;

public class EventCountStreamProcessor implements StreamProcessor<String, String> {

    private static final Logger log = LoggerFactory.getLogger(EventCountStreamProcessor.class.getSimpleName());

    private static final String TIME_SERIES_STORE = "event-count-store";
    private static final String TIME_SERIES_TOPIC = "wikimedia.stats.timeseries";

    private static final String START_TIME_PROPERTY = "start_time";
    private static final String END_TIME_PROPERTY = "end_time";
    private static final String WINDOW_SIZE_PROPERTY = "window_size";
    private static final String EVENT_COUNT_PROPERTY = "event_count";

    private final ObjectMapper mapper;
    private final Duration windowDuration;

    public EventCountStreamProcessor(ObjectMapper mapper, Duration windowDuration) {
        this.mapper = mapper;
        this.windowDuration = windowDuration;
    }

    @Override
    public void setUp(KStream<String, String> stream) {
        final var timeWindows = TimeWindows.ofSizeWithNoGrace(windowDuration);
        stream.selectKey((k, v) -> "key-to-group")
                .groupByKey()
                .windowedBy(timeWindows)
                .count(Materialized.as(TIME_SERIES_STORE))
                .toStream()
                .mapValues((readOnlyKey, value) -> {
                    final var kvMap = Map.of(
                            START_TIME_PROPERTY, readOnlyKey.window().startTime().toString(),
                            END_TIME_PROPERTY, readOnlyKey.window().endTime().toString(),
                            WINDOW_SIZE_PROPERTY, timeWindows.size(),
                            EVENT_COUNT_PROPERTY, value
                    );
                    try {
                        return mapper.writeValueAsString(kvMap);
                    } catch (JsonProcessingException e) {
                        log.error("Error serializing time series info", e);
                        return null;
                    }
                })
                .to(TIME_SERIES_TOPIC, Produced.with(
                        WindowedSerdes.timeWindowedSerdeFrom(String.class, timeWindows.size()),
                        Serdes.String()
                ));
    }

}
