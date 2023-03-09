package dev.sotoestevez.search;

import dev.sotoestevez.indices.SearchIndex;

import java.io.IOException;

public interface SearchClient extends AutoCloseable {

    void createIndex(String name) throws IOException;

    <T> void insertDocument(SearchIndex<T> index, String document) throws IOException;

}
