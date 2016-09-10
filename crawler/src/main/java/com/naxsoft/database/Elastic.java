package com.naxsoft.database;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.ProductEntity;
import org.apache.commons.io.IOUtils;
import org.asynchttpclient.Response;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Singleton
public class Elastic implements AutoCloseable, Cloneable {
    private static final int BATCH_SIZE = 32;
    private static final Logger LOGGER = LoggerFactory.getLogger(Elastic.class);
    private TransportClient client = null;

    private String hostname;

    /**
     * Connect to ElasticSearch server
     *
     * @param hostname ElasticSearch server hostname
     * @param port     ElasticSearch server port
     * @throws UnknownHostException
     */
    public void connect(String hostname, int port) throws UnknownHostException {
        if (null == client) {
            this.hostname = hostname;

            Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch").put("client.transport.sniff", true).build();
            this.client = new TransportClient.Builder().settings(settings).build();
            this.client.addTransportAddress(new InetSocketTransportAddress(java.net.InetAddress.getByName(hostname), port));

            while (true) {
                LOGGER.info("Waiting for elastic to connect to a node {}:{}...", hostname, port);
                int connectedNodes = this.client.connectedNodes().size();
                if (0 != connectedNodes) {
                    LOGGER.info("Connection established");
                    break;
                }
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5L));
                } catch (InterruptedException e) {
                    LOGGER.error("Thread sleep failed", e);
                }
            }
        }
    }

    /**
     * Close connection to ElasticSearch server
     */
    public void close() {
        if (null != client) {
            client.close();
            client = null;
            hostname = null;
        }
    }

    /**
     * @return
     */
    public Client getClient() {
        return this.client;
    }

    /**
     * @param products
     * @param indexName
     * @param type
     * @return
     */
    public Observable<Boolean> index(Observable<ProductEntity> products, String indexName, String indexSuffix, String type) {
        return products.buffer(BATCH_SIZE).flatMap(list -> {
            BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
            for (ProductEntity p : list) {
                try {
                    LOGGER.trace("Preparing for indexing {}", p);
                    String fullIndexName = indexName;
                    if (indexSuffix != null) {
                        fullIndexName += indexSuffix;
                    }
                    XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
                    jsonBuilder.startObject();
                    IndexRequestBuilder request = client.prepareIndex(fullIndexName, type, "" + p.getUrl());
                    request.setSource(p.getJson());
                    request.setOpType(IndexRequest.OpType.INDEX);
                    bulkRequestBuilder.add(request);
                } catch (IOException e) {
                    LOGGER.error("Failed to create JSON generator", e);
                }
            }
            return Observable.from(bulkRequestBuilder.execute());
        }).map(bulkResponse -> {
            if (bulkResponse.hasFailures()) {
                LOGGER.error("Failed to index products:{}", bulkResponse.buildFailureMessage());
            } else {
                LOGGER.info("Successfully indexed {} in {}ms", bulkResponse.getItems().length, bulkResponse.getTookInMillis());
            }
            return !bulkResponse.hasFailures();
        });
    }

    /**
     * @param httpClient
     * @param indexName
     * @param type
     * @param indexSuffix
     * @return
     */
    public Observable<Integer> createIndex(HttpClient httpClient, String indexName, String type, String indexSuffix) {
        String resourceName = "/elastic." + indexName + "." + type + ".index.json";
        InputStream resourceAsStream = this.getClass().getResourceAsStream(resourceName);
        try {
            if (!client.admin().indices().exists(new IndicesExistsRequest(indexName)).get().isExists()) {
                if (indexSuffix != null) {
                    indexName = indexName + indexSuffix;
                }
                LOGGER.info("Creating index {} type {} from {}", indexName, type, resourceName);
                String indexContent = IOUtils.toString(resourceAsStream, Charset.forName("UTF-8"));
                String url = "http://" + hostname + ":9200/" + indexName;
                return httpClient.post(url, indexContent, new VoidAbstractCompletionHandler());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to create index", e);
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
        return Observable.empty();
    }

    /**
     * @param client
     * @param indexName
     * @param type
     * @param indexSuffix
     * @return
     */
    public Observable<Integer> createMapping(HttpClient client, String indexName, String type, String indexSuffix) {
        String resourceName = "/elastic." + indexName + "." + type + ".mapping.json";
        InputStream resourceAsStream = this.getClass().getResourceAsStream(resourceName);
        try {
            if (indexSuffix != null) {
                indexName = indexName + indexSuffix;
            }
            LOGGER.info("Creating mapping for index {} type {} from {}", indexName, type, resourceName);

            String indexContent = IOUtils.toString(resourceAsStream, Charset.forName("UTF-8"));
            String url = "http://" + hostname + ":9200/" + indexName + "/" + type + "/_mapping";

            return client.post(url, indexContent, new VoidAbstractCompletionHandler());
        } catch (IOException e) {
            LOGGER.error("Failed to create mapping", e);
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
        return Observable.empty();
    }

    /**
     * @param index
     * @param newAlias
     * @param oldAlias
     * @return
     */
    public ListenableActionFuture<IndicesAliasesResponse> updateAlias(String index, String newAlias, String oldAlias) {
        return client.admin().indices().prepareAliases().addAlias(index, newAlias).removeAlias(index, oldAlias).execute();
    }

    private static class VoidAbstractCompletionHandler extends AbstractCompletionHandler<Integer> {
        @Override
        public Integer onCompleted(Response response) throws Exception {
            int statusCode = response.getStatusCode();
            if (200 != statusCode) {
                LOGGER.error("Error creating index: {}", response.getResponseBody());
            } else {
                LOGGER.info("Created index: {}", response.getResponseBody());
            }
            return statusCode;
        }
    }

}

