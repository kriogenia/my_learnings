package dev.sotoestevez.kafka.stream;

import dev.sotoestevez.kafka.stream.builder.WikimediaStatsStreamBuilder;

public class Main {

    private static final String APP_ID = "wikimedia-stats-application";
    private static final String INPUT_TOPIC = "wikimedia.recentchange";

    public static void main(String[] args) {
        var builder = new WikimediaStatsStreamBuilder(APP_ID, INPUT_TOPIC);
        WikimediaStatsStreamBuilder.DEFAULT_STATS_PROCESSORS.forEach(builder::withProcessor);

        try (var streams = builder.build(); streams) {
            streams.start();
            Thread.sleep(30_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}