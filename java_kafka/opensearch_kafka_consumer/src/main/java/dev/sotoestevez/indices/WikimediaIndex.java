package dev.sotoestevez.indices;

import dev.sotoestevez.wikimedia.RecentChange;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;

import java.io.IOException;

public class WikimediaIndex implements SearchIndex<RecentChange> {

    private static final String NAME = "wikimedia";

    private static final JacksonJsonpMapper mapper = new JacksonJsonpMapper();

    @Override
    public String name() {
        return NAME;
    }

    public RecentChange deserialize(String document) throws IOException {
        return mapper.objectMapper().readValue(document, RecentChange.class);
    }

}
