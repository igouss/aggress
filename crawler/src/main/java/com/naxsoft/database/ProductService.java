package com.naxsoft.database;

import com.naxsoft.entity.Product;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class ProductService {
    private final Logger logger;
    private Elasitic elasitic;

    public ProductService(Elasitic elasitic) {
        this.elasitic = elasitic;
        logger = LoggerFactory.getLogger(Database.class);
    }

    public void save(Set<Product> products) {
        Client client = elasitic.getClient();
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        try {
            int i = 0;
            for (Product p : products) {
                XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
                jsonBuilder.startObject();
                IndexRequestBuilder request = client.prepareIndex("product", "guns", "" + p.getId());
                p.getProperties().forEach((k, v) -> {
                    try {
                        jsonBuilder.field(k, v);
                    } catch (IOException e) {
                        logger.error("Failed generating JSON");
                    }
                });
                jsonBuilder.endObject();
                request.setSource(jsonBuilder);
                request.setOpType(IndexRequest.OpType.INDEX);
                bulkRequest.add(request);
                if (++i % 100 == 0) {
                    BulkResponse bulkResponse = bulkRequest.execute().actionGet();
                    if (bulkResponse.hasFailures()) {
                        logger.error("Failed to index products");
                    } else {
                        logger.info("Successfully indexed " + bulkResponse.getItems().length + " in " + bulkResponse.getTookInMillis() + "ms");
                    }
                }
            }
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                logger.error("Failed to index products");
            } else {
                logger.info("Successfully indexed " + bulkResponse.getItems().length + " in " + bulkResponse.getTookInMillis() + "ms");
            }
        } catch (IOException e) {
            logger.error("Failed to save products", e);
        }
    }
}
