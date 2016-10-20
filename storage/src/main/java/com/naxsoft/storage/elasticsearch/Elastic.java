package com.naxsoft.storage.elasticsearch;

import com.naxsoft.entity.ProductEntity;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.AsyncEmitter;
import rx.Observable;

import javax.inject.Singleton;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Singleton
public class Elastic implements AutoCloseable, Cloneable {
    private static final int BATCH_SIZE = 32;
    private static final Logger LOGGER = LoggerFactory.getLogger(Elastic.class);
    private final Random rnd = new Random(System.currentTimeMillis());
    private TransportClient client = null;

    /**
     * Connect to ElasticSearch server
     *
     * @param hostname ElasticSearch server hostname
     * @param port     ElasticSearch server port
     * @throws UnknownHostException When unable to find ES host
     */
    public void connect(String hostname, int port) throws UnknownHostException {
        if (null == client) {
            Settings settings = Settings.settingsBuilder().put("cluster.name", "elasticsearch").put("client.transport.sniff", true).build();
            client = new TransportClient.Builder().settings(settings).build();
            client.addTransportAddress(new InetSocketTransportAddress(java.net.InetAddress.getByName(hostname), port));

            while (true) {
                LOGGER.info("Waiting for elastic to connect to a node {}:{}...", hostname, port);
                List<DiscoveryNode> discoveryNodes = client.connectedNodes();
                if (0 != discoveryNodes.size()) {
                    LOGGER.info("Connection established {}", discoveryNodes.stream().map(DiscoveryNode::toString).reduce("", (a, b) -> {
                        if (a.isEmpty()) {
                            return b;
                        } else {
                            return a + ", " + b;
                        }
                    }));
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
        }
    }

    /**
     * Bulk insert data to ES.
     *
     * @param product   Data to insert
     * @param indexName Target ES index
     * @param type      Target ES type
     * @return Results of bulk insertion
     */
    public Observable<Boolean> index(ProductEntity product, String indexName, String type) {
//        LOGGER.info("Preparing for indexing {} elements", product);
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        try {
            XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
            jsonBuilder.startObject();
            IndexRequestBuilder request = client.prepareIndex(indexName, type, product.getUrl());
            request.setSource(product.getJson());
            request.setOpType(IndexRequest.OpType.INDEX);
            bulkRequestBuilder.add(request);
        } catch (Exception e) {
            LOGGER.error("Failed to generate bulk add operation", e);
        }

        return Observable.from(bulkRequestBuilder.execute()).map(bulkResponse -> {
            if (bulkResponse.hasFailures()) {
                LOGGER.error("Failed to index products:{}", bulkResponse.buildFailureMessage());
            } else {
                LOGGER.info("Successfully indexed {} in {}ms", bulkResponse.getItems().length, bulkResponse.getTookInMillis());
            }
            return !bulkResponse.hasFailures();
        });
    }

    /**
     * Create a new ES index if it does not exist already.
     * Index definition is loaded from resource file "/elastic." + indexName + "." + type + ".index.json"
     *
     * @param indexName Name of the ES index
     * @param type      ES index type
     * @return Observable that either completes or errors.
     */
    public Observable<Boolean> createIndex(String indexName, String type) {
        return Observable.fromEmitter(emitter -> {
            String resourceName = "/elastic." + indexName + "." + type + ".index.json";
            InputStream resourceAsStream = this.getClass().getResourceAsStream(resourceName);
            try {
                LOGGER.info("Creating index {} type {} from {}", indexName, type, resourceName);
                if (!indexExists(indexName)) {
                    Settings settings = Settings.builder().loadFromStream(resourceName, resourceAsStream).build();
                    CreateIndexRequest request = new CreateIndexRequest(indexName, settings);

                    client.admin().indices().create(request, new ActionListener<CreateIndexResponse>() {
                        @Override
                        public void onResponse(CreateIndexResponse createIndexResponse) {
//                            LOGGER.info("Index created {}", createIndexResponse.isAcknowledged());
                            emitter.onNext(createIndexResponse.isAcknowledged());
                            emitter.onCompleted();
                        }

                        @Override
                        public void onFailure(Throwable e) {
                            LOGGER.error("Failed to create index", e);
                            emitter.onError(e);
                        }
                    });
                } else {
                    LOGGER.info("Index already exists");
                    emitter.onCompleted();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to create index", e);
                emitter.onError(e);
            } finally {
                IOUtils.closeQuietly(resourceAsStream);
            }
        }, AsyncEmitter.BackpressureMode.LATEST);
    }

    private boolean indexExists(String indexName) throws InterruptedException, java.util.concurrent.ExecutionException {
        return client.admin().indices().exists(new IndicesExistsRequest(indexName)).get().isExists();
    }

    public Observable<Boolean> price_index(ProductEntity product, String indexName, String type) {
//        LOGGER.info("Preparing for indexing {} elements", product);
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        try {
            XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
            jsonBuilder.startObject();
            jsonBuilder.field("url", product.getUrl());
            jsonBuilder.field("crawlDate", Date.from(Instant.now()));
            String price;
            if (product.getSpecialPrice() != null && !product.getSpecialPrice().isEmpty()) {
                price = product.getSpecialPrice();
            } else if (product.getRegularPrice() != null && !product.getRegularPrice().isEmpty()) {
                price = product.getRegularPrice();
            } else {
                price = "" + rnd.nextDouble() * 100;
            }
            jsonBuilder.field("price", Double.valueOf(price));
            jsonBuilder.endObject();

            IndexRequestBuilder request = client.prepareIndex(indexName, type);
            request.setSource(jsonBuilder);
            request.setOpType(IndexRequest.OpType.CREATE);
            bulkRequestBuilder.add(request);
        } catch (Exception e) {
            LOGGER.error("Failed to generate bulk add operation", e);
        }

        return Observable.from(bulkRequestBuilder.execute()).map(bulkResponse -> {
            if (bulkResponse.hasFailures()) {
                LOGGER.error("Failed to price index products:{}", bulkResponse.buildFailureMessage());
            } else {
                LOGGER.info("Successfully price indexed {} in {}ms", bulkResponse.getItems().length, bulkResponse.getTookInMillis());
            }
            return !bulkResponse.hasFailures();
        });

    }
}
