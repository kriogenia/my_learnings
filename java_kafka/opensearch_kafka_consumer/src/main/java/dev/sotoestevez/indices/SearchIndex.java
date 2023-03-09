package dev.sotoestevez.indices;

import java.io.IOException;

public interface SearchIndex<T> {

    String name();

    T deserialize(String document) throws IOException;

}
