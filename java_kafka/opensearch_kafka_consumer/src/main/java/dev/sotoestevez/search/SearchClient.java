package dev.sotoestevez.search;

import java.io.IOException;

public interface SearchClient extends AutoCloseable {

    void createIndex(String name) throws IOException;

    void insertDocument(String index, String document) throws IOException;
}
