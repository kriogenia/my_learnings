package dev.sotoestevez.kafka.consumer;

import dev.sotoestevez.search.OSSearchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class Main {

    private static String INDEX_NAME = "wikimedia";

    public static void main(String[] args) throws IOException {
        var client = OSSearchClient.newInstance();
        try (client) {
            client.createIndex(INDEX_NAME);
        }
        System.out.println("Hello world");
    }
}
