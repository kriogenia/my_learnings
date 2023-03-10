package dev.sotoestevez.indices;

import java.io.IOException;
import java.util.function.Function;

public interface SearchIndex<T> {

    String name();

    Function<T, String> toId();

    T deserialize(String document) throws IOException;

}
