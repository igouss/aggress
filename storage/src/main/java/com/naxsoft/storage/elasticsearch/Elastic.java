package com.naxsoft.storage.elasticsearch;

import com.naxsoft.common.entity.ProductEntity;
import com.naxsoft.utils.JsonEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Semaphore;

@Slf4j
@RequiredArgsConstructor
public class Elastic {
    private final static Semaphore esConcurrency = new Semaphore(4);
    private final TransportClient client;

    /**
     * Create a new ES index if it does not exist already.
     * Index definition is loaded from resource file "/elastic." + indexName + "." + type + ".index.json"
     *
     * @param indexName Name of the ES index
     * @param type      ES index type
     * @return Observable that either completes or errors.
     */
    public Boolean createIndex(String indexName, String type) {
        String indexFile = "/elastic." + indexName + "." + type + ".index.json";
        InputStream indexResource = this.getClass().getResourceAsStream(indexFile);
        String mappingFile = "/elastic." + indexName + "." + type + ".mapping.json";
        InputStream mappingStream = this.getClass().getResourceAsStream(mappingFile);

        try {
            log.info("Creating index {} type {} from {}", indexName, type, indexFile);
            Settings settings = Settings.builder().loadFromStream(indexFile, indexResource, true).build();

            if (indexExists(indexName)) {
                DeleteIndexResponse deleteIndexResponse = client.admin().indices().delete(Requests.deleteIndexRequest(indexName)).actionGet();
                if (deleteIndexResponse.isAcknowledged()) {
                    log.info("Index deleted");
                } else {
                    log.error("Index deleted failed");
                }
            }

            CreateIndexRequest request = Requests.createIndexRequest(indexName);
            request.settings(settings);

            CreateIndexResponse createIndexResponse = client.admin().indices().create(request).actionGet();
            if (createIndexResponse.isShardsAcknowledged()) {
                log.info("Index created {}", indexName);

                PutMappingRequest putMappingRequest = Requests.putMappingRequest(indexName);
                putMappingRequest.source(IOUtils.toString(mappingStream, StandardCharsets.UTF_8), XContentType.JSON);
                putMappingRequest.type("guns");
                PutMappingResponse putMappingResponse = client.admin().indices().putMapping(putMappingRequest).actionGet();
                if (putMappingResponse.isAcknowledged()) {
                    log.info("Mapping created");
                } else {
                    log.error("Mapping failed");
                }
            } else {
                log.error("Failed to create index");
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to create index", e);
            return false;
        } finally {
            try {
                indexResource.close();
                mappingStream.close();
            } catch (IOException e) {
                log.error("Failed to close resources", e);
            }
        }
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
    public boolean index(List<ProductEntity> products, String indexName, String type) {
//        log.info("Preparing for indexing {} elements", product);
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        try {
            for (ProductEntity product : products) {
                XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
                jsonBuilder.startObject();
                IndexRequestBuilder request = client.prepareIndex(indexName, type, DigestUtils.sha1Hex(product.getUrl() + product.getProductName()));
                log.info("Preparing to index {}/{} value {}", indexName, type, product.getUrl());
                request.setSource(JsonEncoder.toJson(product), XContentType.JSON);
                request.setOpType(IndexRequest.OpType.INDEX);
                bulkRequestBuilder.add(request);
            }
        } catch (Exception e) {
            log.error("Failed to generate bulk add operation", e);
        }

        return false;

//        return Observable.create(emitter -> {
//            try {
//                esConcurrency.acquire();
//                bulkRequestBuilder.execute(new ActionListener<BulkResponse>() {
//                    @Override
//                    public void onResponse(BulkResponse bulkItemResponses) {
//                        if (bulkItemResponses.hasFailures()) {
//                            log.error("Failed to index products:{}", bulkItemResponses.buildFailureMessage());
//                        } else {
//                            log.info("Successfully indexed {} in {}ms", bulkItemResponses.getItems().length, bulkItemResponses.getIngestTookInMillis());
//                        }
//                        emitter.onNext(!bulkItemResponses.hasFailures());
//                        emitter.onCompleted();
//                        esConcurrency.release();
//                    }
//
//                    @Override
//                    public void onFailure(Exception e) {
//                        emitter.onError(e);
//
//                    }
//                });
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }, Emitter.BackpressureMode.BUFFER);
    }

    public boolean price_index(List<ProductEntity> products, String indexName, String type) {
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();

        try {
            for (ProductEntity product : products) {
                XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
                log.info("Preparing to index {}/{} value {}", indexName, type, product.getUrl());
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
                    log.warn("Unable to find price");
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
        } catch (Exception e) {
            log.error("Failed to generate bulk add operation", e);
        }

        return false;

//        return Observable.create(emitter -> {
//            try {
//                esConcurrency.acquire();
//                bulkRequestBuilder.execute(new ActionListener<BulkResponse>() {
//                    @Override
//                    public void onResponse(BulkResponse bulkItemResponses) {
//                        if (bulkItemResponses.hasFailures()) {
//                            log.error("Failed to price index products:{}", bulkItemResponses.buildFailureMessage());
//                        } else {
//                            log.info("Successfully price indexed {} in {}ms", bulkItemResponses.getItems().length, bulkItemResponses.getIngestTookInMillis());
//                        }
//                        emitter.onNext(!bulkItemResponses.hasFailures());
//                        emitter.onCompleted();
//                        esConcurrency.release();
//                    }
//
//                    @Override
//                    public void onFailure(Exception e) {
//                        emitter.onError(e);
//                    }
//                });
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }, Emitter.BackpressureMode.BUFFER);
    }
}
