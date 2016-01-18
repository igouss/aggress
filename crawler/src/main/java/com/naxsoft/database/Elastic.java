//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.crawler.CompletionHandler;
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
    private static final Logger logger = LoggerFactory.getLogger(Elastic.class);
    private TransportClient client = null;

    /**
     *
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
                logger.info("Waiting for elastic to connect to a node...");
                int connectedNodes = this.client.connectedNodes().size();
                if (0 != connectedNodes) {
                    logger.info("Connection established");
                    break;
                }
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5L));
                } catch (InterruptedException e) {
                    logger.error("Thread sleep failed", e);
                }
            }
        }

    }

    /**
     *
     */
    public void close() {
        if (null != client) {
            this.client.close();
        }
    }

    /**
     *
     * @return
     */
    public Client getClient() {
        return this.client;
    }


    /**
     *
     * @param products
     * @param index
     * @param type
     * @return
     */
    public Subscription index(Observable<ProductEntity> products, String index, String type) {
        return products.buffer(200).map(list -> {
            BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
            for (ProductEntity p : list) {
                try {
                    XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
                    jsonBuilder.startObject();
                    IndexRequestBuilder request = client.prepareIndex(index, type, "" + p.getUrl());
                    request.setSource(p.getJson());
                    request.setOpType(IndexRequest.OpType.INDEX);
                    bulkRequestBuilder.add(request);
                } catch (IOException e) {
                    logger.error("Failed to create JSON generator", e);
                }
            }
            return bulkRequestBuilder.execute();
        }).flatMap(Observable::from)
                .retry(3)
                .subscribe(bulkResponse -> {
                    if (bulkResponse.hasFailures()) {
                        logger.error("Failed to index products:{}", bulkResponse.buildFailureMessage());
                    } else {
                        logger.info("Successfully indexed {} in {}ms", bulkResponse.getItems().length, bulkResponse.getTookInMillis());
                    }
                }, ex -> logger.error("Index Exception", ex));
    }

    /**
     *
     * @param client
     * @param index
     * @param type
     * @param indexSuffix
     * @return
     */
    public Observable<Integer> createIndex(HttpClient client, String index, String type, String indexSuffix) {
        return Observable.create(subscriber -> {
            try {
                String resourceName = "/elastic." + index + "." + type + ".index.json";
                String newIndexName = index + indexSuffix;
                logger.info("Creating index {} type {} from {}", newIndexName, type, resourceName);
                InputStream resourceAsStream = this.getClass().getResourceAsStream(resourceName);
                String indexContent = null;
                indexContent = IOUtils.toString(resourceAsStream);
                String url = "http://127.0.0.1:9200/" + newIndexName;
                client.post(url, indexContent, new CompletionHandler<Void>() {
                    @Override
                    public Void onCompleted(Response response) throws Exception {
                        int statusCode = response.getStatusCode();
                        if (200 != statusCode) {
                            logger.error("Error creating index: {}", response.getResponseBody());
                        } else {
                            logger.info("Created index: {}", response.getResponseBody());
                        }
                        subscriber.onNext(statusCode);
                        subscriber.onCompleted();
                        return null;
                    }
                });
            } catch (IOException e) {
                subscriber.onError(e);
            }
        });
    }

    /**
     *
     * @param client
     * @param index
     * @param type
     * @param indexSuffix
     * @return
     */
    public Observable<Integer> createMapping(HttpClient client, String index, String type, String indexSuffix) {
        return Observable.create(subscriber -> {
            try {
                String resourceName = "/elastic." + index + "." + type + ".mapping.json";
                String newIndexName = index + indexSuffix;
                logger.info("Creating mapping for index {} type {} from {}", newIndexName, type, resourceName);
                InputStream resourceAsStream = this.getClass().getResourceAsStream(resourceName);
                String indexContent = IOUtils.toString(resourceAsStream);
                String url = "http://localhost:9200/" + newIndexName + "/" + type + "/_mapping";

                client.post(url, indexContent, new CompletionHandler<Integer>() {
                    @Override
                    public Integer onCompleted(Response response) throws Exception {
                        int statusCode = response.getStatusCode();
                        if (200 != statusCode) {
                            logger.error("Error creating mapping: {}", response.getResponseBody());
                        } else {
                            logger.info("Created mapping: {}", response.getResponseBody());
                        }
                        subscriber.onNext(statusCode);
                        subscriber.onCompleted();
                        return null;
                    }
                });
            } catch (IOException e) {
                subscriber.onError(e);
            }
        });
    }

    /**
     *
     * @param index
     * @param newAlias
     * @param oldAlias
     * @return
     */
    public ListenableActionFuture<IndicesAliasesResponse> updateAlias(String index, String newAlias, String oldAlias) {
        return client.admin().indices().prepareAliases().addAlias(index, newAlias).removeAlias(index, oldAlias).execute();
    }
}

