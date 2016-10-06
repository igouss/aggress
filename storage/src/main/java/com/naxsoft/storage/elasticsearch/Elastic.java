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
import java.util.concurrent.TimeUnit;

/**
 *
 */
@Singleton
public class Elastic implements AutoCloseable, Cloneable {
    private static final int BATCH_SIZE = 32;
    private static final Logger LOGGER = LoggerFactory.getLogger(Elastic.class);
    private TransportClient client = null;

    /**
     * Connect to ElasticSearch server
     *
     * @param hostname ElasticSearch server hostname
     * @param port     ElasticSearch server port
     * @throws UnknownHostException
     */
    public void connect(String hostname, int port) throws UnknownHostException {
        if (null == client) {

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
        }
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
     * @param indexName
     * @param type
     * @param indexSuffix
     * @return
     */
    public Observable<Integer> createIndex(String indexName, String type, String indexSuffix) {
        String resourceName = "/elastic." + indexName + "." + type + ".index.json";
        InputStream resourceAsStream = this.getClass().getResourceAsStream(resourceName);
        try {
            LOGGER.info("Creating index {} type {} from {}", indexName, type, resourceName);
            if (!indexExists(indexName)) {
                if (indexSuffix != null) {
                    indexName = indexName + indexSuffix;
                }

//                String indexContent = IOUtils.toString(resourceAsStream, Charset.forName("UTF-8"));
//                String url = "http://" + hostname + ":9200/" + indexName;

                Settings settings = Settings.builder().loadFromStream(resourceName, resourceAsStream).build();
                CreateIndexRequest request = new CreateIndexRequest(indexName, settings);

                client.admin().indices().create(request, new ActionListener<CreateIndexResponse>() {
                    @Override
                    public void onResponse(CreateIndexResponse createIndexResponse) {
                        LOGGER.info("OK");
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        LOGGER.error("Failed to create index", e);
                    }
                });
            } else {
                LOGGER.info("Index already exists");
            }
        } catch (Exception e) {
            LOGGER.error("Failed to create index", e);
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
        return Observable.empty();
    }

    private boolean indexExists(String indexName) throws InterruptedException, java.util.concurrent.ExecutionException {
        return client.admin().indices().exists(new IndicesExistsRequest(indexName)).get().isExists();
    }
}

