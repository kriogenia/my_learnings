package dev.sotoestevez.model.wikimedia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RecentChange(
        @JsonProperty("$schema") String schema,
        @JsonProperty("meta") Meta meta,
        @JsonProperty("type") String type,
        @JsonProperty("title") String title,
        @JsonProperty("user") String user
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Meta(
            @JsonProperty("uri") String uri,
            @JsonProperty("request_id") String requestId,
            @JsonProperty("id") String id,
            @JsonProperty("domain") String domain
    ) {

    }
}

