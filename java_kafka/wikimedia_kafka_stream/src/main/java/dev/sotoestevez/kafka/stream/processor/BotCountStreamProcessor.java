package dev.sotoestevez.kafka.stream.processor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BotCountStreamProcessor implements StreamProcessor<String, String> {

    private static final Logger log = LoggerFactory.getLogger(BotCountStreamProcessor.class.getSimpleName());

    private static final String BOT_COUNT_STORE = "bot-count-store";
    private static final String BOT_COUNT_TOPIC = "wikimedia.stats.bots";

    private final ObjectMapper mapper;

    public BotCountStreamProcessor(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void setUp(KStream<String, String> stream) {
        stream.mapValues(json -> {
                    try {
                        return mapper.readValue(json, BotInfo.class).isBot ? "bot" : "non-bot";
                    } catch (JsonProcessingException e) {
                        log.warn("Error parsing bot info: {}", e.getMessage());
                        return "invalid";
                    }
                })
                .groupBy((key, botInfo) -> botInfo)
                .count(Materialized.as(BOT_COUNT_STORE))
                .toStream()
                .to(BOT_COUNT_TOPIC, Produced.with(Serdes.String(), Serdes.Long()));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record BotInfo(
            @JsonProperty(value = "bot", defaultValue = "false") boolean isBot
    ) {
    }


}
