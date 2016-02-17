//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.ProductEntity;
import com.ning.http.client.Response;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
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
import rx.Subscription;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class Elastic implements AutoCloseable, Cloneable {
    private static final Logger LOGGER = LoggerFactory.getLogger(Elastic.class);
    public static final int BATCH_SIZE = 1;
    private TransportClient client = null;

    /**
     * @param hostname
     * @param port
     * @throws UnknownHostException
     */
    public void connect(String hostname, int port) throws UnknownHostException {
        if (null == client) {
            Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch").put("client.transport.sniff", true).build();
            this.client = new TransportClient.Builder().settings(settings).build();
            this.client.addTransportAddress(new InetSocketTransportAddress(java.net.InetAddress.getByName(hostname), port));

            while (true) {
                LOGGER.info("Waiting for elastic to connect to a node...");
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
            this.client.close();
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
     * @param index
     * @param type
     * @return
     */
    public Subscription index(Observable<ProductEntity> products, String index, String type) {
        return products.buffer(BATCH_SIZE).map(list -> {
            BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
            for (ProductEntity p : list) {
                try {
                    XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
                    jsonBuilder.startObject();
                    IndexRequestBuilder request = client.prepareIndex(index, type, "" + p.getUrl());
                    String payload = p.getJson();
                    request.setSource(payload);
                    request.setOpType(IndexRequest.OpType.INDEX);
                    bulkRequestBuilder.add(request);
                } catch (IOException e) {
                    LOGGER.error("Failed to create JSON generator", e);
                }
            }
            return bulkRequestBuilder.execute();
        }).flatMap(Observable::from)
                .retry(3)
                .subscribe(bulkResponse -> {
                    if (bulkResponse.hasFailures()) {
                        LOGGER.error("Failed to index products:{}", bulkResponse.buildFailureMessage());
                    } else {
                        LOGGER.info("Successfully indexed {} in {}ms", bulkResponse.getItems().length, bulkResponse.getTookInMillis());
                    }
                }, ex -> LOGGER.error("Index Exception", ex));
    }

    /**
     * @param client
     * @param index
     * @param type
     * @param indexSuffix
     * @return
     */
    public Observable<Integer> createIndex(HttpClient client, String index, String type, String indexSuffix) {
        String resourceName = "/elastic." + index + "." + type + ".index.json";
        InputStream resourceAsStream = this.getClass().getResourceAsStream(resourceName);
        try {
            String newIndexName = index + indexSuffix;
            LOGGER.info("Creating index {} type {} from {}", newIndexName, type, resourceName);

            String indexContent = null;
            indexContent = IOUtils.toString(resourceAsStream);
            String url = "http://127.0.0.1:9200/" + newIndexName;
            return Observable.from(client.post(url, indexContent, new VoidAbstractCompletionHandler()));
        } catch (Exception e) {
            LOGGER.error("Failed to create index", e);
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
        return Observable.empty();
    }

    /**
     * @param client
     * @param index
     * @param type
     * @param indexSuffix
     * @return
     */
    public Observable<Integer> createMapping(HttpClient client, String index, String type, String indexSuffix) {
        String resourceName = "/elastic." + index + "." + type + ".mapping.json";
        InputStream resourceAsStream = this.getClass().getResourceAsStream(resourceName);
        try {

            String newIndexName = index + indexSuffix;
            LOGGER.info("Creating mapping for index {} type {} from {}", newIndexName, type, resourceName);

            String indexContent = IOUtils.toString(resourceAsStream);
            String url = "http://localhost:9200/" + newIndexName + "/" + type + "/_mapping";

            return Observable.from(client.post(url, indexContent, new VoidAbstractCompletionHandler()));
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

        public VoidAbstractCompletionHandler() {
        }

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
//
//    private static class IntegerAbstractCompletionHandler extends AbstractCompletionHandler<Integer> {
//        private final Subscriber<? super Integer> subscriber;
//
//        public IntegerAbstractCompletionHandler(Subscriber<? super Integer> subscriber) {
//            this.subscriber = subscriber;
//        }
//
//        @Override
//        public Integer onCompleted(Response response) throws Exception {
//            int statusCode = response.getStatusCode();
//            if (200 != statusCode) {
//                LOGGER.error("Error creating mapping: {}", response.getResponseBody());
//            } else {
//                LOGGER.info("Created mapping: {}", response.getResponseBody());
//            }
//            subscriber.onNext(statusCode);
//            subscriber.onCompleted();
//            return null;
//        }
//    }
}

