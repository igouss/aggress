package com.naxsoft.storage.elasticsearch;

import com.naxsoft.entity.ProductEntity;
import io.reactivex.Flowable;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Singleton
public class Elastic implements AutoCloseable, Cloneable {
    private static final int BATCH_SIZE = 32;
    private static final Logger LOGGER = LoggerFactory.getLogger("elastic");
    private final static Semaphore esConcurrency = new Semaphore(4);
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
            Settings settings = Settings.builder().put("cluster.name", "elasticsearch").put("client.transport.sniff", true).build();
            client = new PreBuiltTransportClient(settings)
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostname), port));

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
            LOGGER.info("Shutting down elastic");
            client.close();
            client = null;
        }
    }

    /**
     * Create a new ES index if it does not exist already.
     * Index definition is loaded from resource file "/elastic." + indexName + "." + type + ".index.json"
     *
     * @param indexName Name of the ES index
     * @param type      ES index type
     * @return Flowable that either completes or errors.
     */
    public Flowable<Boolean> createIndex(String indexName, String type) {
        String resourceName = "/elastic." + indexName + "." + type + ".index.json";
        InputStream resourceAsStream = this.getClass().getResourceAsStream(resourceName);
        try {
            LOGGER.info("Creating index {} type {} from {}", indexName, type, resourceName);
            if (!indexExists(indexName)) {
                Settings settings = Settings.builder().loadFromStream(resourceName, resourceAsStream).build();
                CreateIndexRequest request = new CreateIndexRequest(indexName, settings);
                return Flowable.fromFuture(client.admin().indices().create(request)).map(AcknowledgedResponse::isAcknowledged);
            } else {
                LOGGER.info("Index already exists");

            }
        } catch (Exception e) {
            LOGGER.error("Failed to create index", e);
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
        return Flowable.just(false);
    }

    private boolean indexExists(String indexName) throws InterruptedException, java.util.concurrent.ExecutionException {
        return client.admin().indices().exists(new IndicesExistsRequest(indexName)).get().isExists();
    }


    /**
     * Bulk insert data to ES.
     *
     * @param products  Data to insert
     * @param indexName Target ES index
     * @param type      Target ES type
     * @return Results of bulk insertion
     */
    public Flowable<Boolean> index(ProductEntity product, String indexName, String type) {

        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        try {
            XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
            jsonBuilder.startObject();
            IndexRequestBuilder request = client.prepareIndex(indexName, type, DigestUtils.sha1Hex(product.getUrl() + product.getProductName()));
            LOGGER.info("Preparing to index {}/{} value {}", indexName, type, product.getUrl());
            request.setSource(product.getJson());
            request.setOpType(IndexRequest.OpType.INDEX);
            bulkRequestBuilder.add(request);
            return Flowable.just(bulkRequestBuilder.execute().get()).map(bulkItemResponses -> !bulkItemResponses.hasFailures());
        } catch (Exception e) {
            LOGGER.error("Failed to index", e);
        }
        return Flowable.just(false);
    }

    /**
     * @param products
     * @param indexName
     * @param type
     * @return
     */
    public Flowable<Boolean> price_index(List<ProductEntity> products, String indexName, String type) {
        if (products == null || products.size() == 0) {
            return Flowable.just(false);
        }
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        try {
            for (ProductEntity product : products) {
                XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
                LOGGER.info("Preparing to index {}/{} value {}", indexName, type, product.getUrl());
                jsonBuilder.startObject();
                jsonBuilder.field("url", product.getUrl());
                jsonBuilder.field("crawlDate", Date.from(Instant.now()));
                String price = "N/A";
                if (product.getSpecialPrice() != null && !product.getSpecialPrice().isEmpty()) {
                    price = product.getSpecialPrice();
                } else if (product.getRegularPrice() != null && !product.getRegularPrice().isEmpty()) {
                    price = product.getRegularPrice();
                }
                if (price.equals("N/A")) {
                    LOGGER.warn("Unable to find price");
                    continue;
                }
                jsonBuilder.field("price", Double.valueOf(price));
                jsonBuilder.endObject();

                String id = DigestUtils.sha1Hex(product.getUrl() + product.getProductName() + price);
                IndexRequestBuilder request = client.prepareIndex(indexName, type, id);
                request.setSource(jsonBuilder);
                request.setOpType(IndexRequest.OpType.CREATE);
                bulkRequestBuilder.add(request);
            }
            if (bulkRequestBuilder.numberOfActions() > 0) {
                return Flowable.just(bulkRequestBuilder.execute().get()).map(bulkItemResponses -> !bulkItemResponses.hasFailures());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to price index", e);
        }
        return Flowable.just(false);
    }
}
