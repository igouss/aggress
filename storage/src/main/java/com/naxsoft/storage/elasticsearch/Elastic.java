package com.naxsoft.storage.elasticsearch;

import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.Date;
import javax.inject.Singleton;
import com.naxsoft.entity.ProductEntity;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
@Singleton
public class Elastic implements AutoCloseable, Cloneable {
    private static final Logger LOGGER = LoggerFactory.getLogger("elastic");
    private RestHighLevelClient client = null;

    /**
     * Connect to ElasticSearch server
     *
     * @param hostname ElasticSearch server hostname
     * @param port     ElasticSearch server port
     * @throws UnknownHostException When unable to find ES host
     */
    public void connect(String hostname, int port) {
        if (null == client) {
            client = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(hostname, port))
            );
        }
    }

    /**
     * Close connection to ElasticSearch server
     */
    public void close() throws IOException {
        if (null != client) {
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
     * @return Observable that either completes or errors.
     */
    public boolean createIndex(String indexName, String type) throws Exception {
        String resourceName = "/elastic." + indexName + "." + type + ".index.json";
        boolean rc = false;
        try (InputStream resourceAsStream = this.getClass().getResourceAsStream(resourceName)) {
            LOGGER.info("Creating index {} type {} from {}", indexName, type, resourceName);
            if (!indexExists(indexName)) {
                Settings settings = Settings.builder().loadFromStream(resourceName, resourceAsStream, false).build();
                CreateIndexRequest request = new CreateIndexRequest(indexName);
                request.settings(settings);
                CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
                rc = createIndexResponse.isAcknowledged();
            } else {
                LOGGER.info("Index already exists");
            }
        }
        return rc;
    }

    private boolean indexExists(String indexName) throws IOException {
        return client.indices().exists(new GetIndexRequest(indexName), RequestOptions.DEFAULT);
    }

    /**
     * Bulk insert data to ES.
     *
     * @param products  Data to insert
     * @param indexName Target ES index
     * @param type      Target ES type
     * @return Results of bulk insertion
     */
    public boolean index(Iterable<ProductEntity> products, String indexName, String type) throws IOException {
        BulkRequest bulkRequest = new BulkRequest(indexName);

        for (ProductEntity product : products) {
            XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
            jsonBuilder.startObject();

            IndexRequest indexRequest = new IndexRequest(indexName);
            indexRequest.source(product.getUrl());
            indexRequest.opType(DocWriteRequest.OpType.INDEX);
            bulkRequest.add(indexRequest);
        }

        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulkResponse.hasFailures();
    }

    /**
     * @param products
     * @param indexName
     * @param type
     * @return
     */
    public boolean priceIndex(Iterable<ProductEntity> products, String indexName, String type) throws IOException {
        BulkRequest bulkRequest = new BulkRequest(indexName);

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

                IndexRequest indexRequest = new IndexRequest(indexName);
                indexRequest.source(jsonBuilder);
                indexRequest.opType(DocWriteRequest.OpType.INDEX);
                bulkRequest.add(indexRequest);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to generate bulk add operation", e);
        }

        BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulkResponse.hasFailures();
    }
}
