package dev.sotoestevez.search;

import java.io.Closeable;
import java.io.IOException;

public interface SearchClient extends Closeable {

    void createIndex(String name) throws IOException;

}
