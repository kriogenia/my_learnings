package dev.sotoestevez.search.opensearch;

import dev.sotoestevez.indices.SearchIndex;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.bulk.IndexOperation;

import java.io.IOException;

public class OSIndexRequester<T> {

    private final SearchIndex<T> index;

    public OSIndexRequester(SearchIndex<T> index) {
        this.index = index;
    }

    public IndexRequest<T> index(String document) throws IOException {
        var indexable = this.index.deserialize(document);
        return new IndexRequest.Builder<T>()
                .index(index.name())
                .document(indexable)
                .id(this.index.toId().apply(indexable))
                .build();
    }

    public IndexOperation<T> indexOperation(String document) throws IOException {
        var indexable = this.index.deserialize(document);
        return new IndexOperation.Builder<T>()
                .index(index.name())
                .document(indexable)
                .id(this.index.toId().apply(indexable))
                .build();
    }

}
