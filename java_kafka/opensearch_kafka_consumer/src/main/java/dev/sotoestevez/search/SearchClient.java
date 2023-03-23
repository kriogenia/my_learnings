package dev.sotoestevez.search;

import dev.sotoestevez.indices.SearchIndex;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public interface SearchClient extends AutoCloseable {

    void createIndex(String name) throws IOException;

    <T> void insertDocument(SearchIndex<T> index, String document) throws IOException;

    <T> void bulkInsertDocument(SearchIndex<T> index, Collection<String> documents) throws IOException;
}
