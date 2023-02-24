package dev.sotoestevez.search;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.indices.CreateIndexRequest;
import org.opensearch.client.opensearch.indices.ExistsRequest;
import org.opensearch.client.opensearch.indices.GetIndexRequest;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;

public class OSSearchClient implements SearchClient, Closeable {

    private static final Logger log = LoggerFactory.getLogger(OSSearchClient.class.getSimpleName());

    private static final URI uri = URI.create("http://localhost:9200");

    private final OpenSearchClient client;
    private final RestClient restClient;

    private OSSearchClient(RestClient restClient) {
        this.restClient = restClient;
        var transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        this.client = new OpenSearchClient(transport);
    }

    public static OSSearchClient newInstance() {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("admin", "admin"));

        //Initialize the client with SSL and TLS enabled
        var client = RestClient.builder(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()))
                .setHttpClientConfigCallback(builder -> builder.setDefaultCredentialsProvider(credentialsProvider))
                .build();
        return new OSSearchClient(client);
    }

    @Override
    public void createIndex(String name) throws IOException {
        if (client.indices().exists(ExistsRequest.of(builder -> builder.index(name))).value()) {
            log.info("Index {} already exists, skipping", name);
            return;
        }
        var request = new CreateIndexRequest.Builder().index(name).build();
        client.indices().create(request);
        log.info("Created index {}", name);
    }

    @Override
    public void close() throws IOException {
        this.restClient.close();
    }
}
