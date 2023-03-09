package dev.sotoestevez.search.opensearch;

import dev.sotoestevez.indices.SearchIndex;
import org.opensearch.client.opensearch.core.IndexRequest;

import java.io.IOException;

public class OSIndexRequester<T> {

    private final SearchIndex<T> index;

    public OSIndexRequester(SearchIndex<T> index) {
        this.index = index;
    }

    public IndexRequest<T> index(String document) throws IOException {
        return new IndexRequest.Builder<T>().index(index.name()).document(
                this.index.deserialize(document)).build();
    }

}
