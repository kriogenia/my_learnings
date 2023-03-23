package dev.sotoestevez.indices;

import dev.sotoestevez.model.wikimedia.RecentChange;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Function;

public class WikimediaIndex implements SearchIndex<RecentChange> {

    private static final String NAME = "wikimedia";

    private static final Logger log = LoggerFactory.getLogger(WikimediaIndex.class.getSimpleName());

    private static final JacksonJsonpMapper mapper = new JacksonJsonpMapper();

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public Function<RecentChange, String> toId() {
        return rc -> rc.meta().id();
    }

    public RecentChange deserialize(String document) throws IOException {
        try {
            log.info(document);
            return mapper.objectMapper().readValue(document, RecentChange.class);
        } catch (IOException ioe) {
            log.error("Error formatting {}", document);
            throw ioe;
        }
        //return mapper.objectMapper().readValue(document, RecentChange.class);
    }

}
