package com.naxsoft.storage.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.naxsoft.entity.ProductEntity;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;

import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Elasticsearch client using Java API Client
 */
@Singleton
public class Elastic implements AutoCloseable, Cloneable {
    private static final int BATCH_SIZE = 32;
    private static final Logger LOGGER = LoggerFactory.getLogger("elastic");
    private final static Semaphore esConcurrency = new Semaphore(4);
    private final Random rnd = new Random(System.currentTimeMillis());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private ElasticsearchClient client = null;
    private RestClient restClient = null;

    /**
     * Connect to ElasticSearch server
     *
     * @param hostname ElasticSearch server hostname
     * @param port     ElasticSearch server port
     */
    public void connect(String hostname, int port) {
        if (null == client) {
            restClient = RestClient.builder(new HttpHost(hostname, port, "http")).build();

            RestClientTransport transport = new RestClientTransport(
                    restClient, new JacksonJsonpMapper()
            );

            client = new ElasticsearchClient(transport);

            // Test connection by trying to ping the cluster
            while (true) {
                LOGGER.info("Waiting for elastic to connect to {}:{}...", hostname, port);
                try {
                    // Try to ping the cluster using info API
                    var response = client.info();
                    if (response != null) {
                        LOGGER.info("Connection established to {}:{}, cluster: {}",
                                hostname, port, response.clusterName());
                        break;
                    }
                } catch (Exception e) {
                    LOGGER.warn("Failed to connect to Elasticsearch: {}", e.getMessage());
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
            try {
                if (restClient != null) {
                    restClient.close();
                }
            } catch (IOException e) {
                LOGGER.error("Failed to close Elasticsearch client", e);
            }
            client = null;
            restClient = null;
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
    public Observable<Boolean> createIndex(String indexName, String type) {
        return Observable.create(emitter -> {
            String resourceName = "/elastic." + indexName + "." + type + ".index.json";
            InputStream resourceAsStream = this.getClass().getResourceAsStream(resourceName);
            try {
                LOGGER.info("Creating index {} type {} from {}", indexName, type, resourceName);
                if (!indexExists(indexName)) {
                    String settingsJson = IOUtils.toString(resourceAsStream, "UTF-8");
                    JsonNode settingsNode = objectMapper.readTree(settingsJson);

                    CompletableFuture.supplyAsync(() -> {
                        try {
                            // Convert JsonNode to InputStream for the API
                            byte[] jsonBytes = objectMapper.writeValueAsBytes(settingsNode);
                            java.io.ByteArrayInputStream inputStream = new java.io.ByteArrayInputStream(jsonBytes);

                            CreateIndexRequest request = CreateIndexRequest.of(b -> b
                                    .index(indexName)
                                    .withJson(inputStream)
                            );

                            CreateIndexResponse response = client.indices().create(request);
                            return response.acknowledged();
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).whenComplete((acknowledged, throwable) -> {
                        if (throwable != null) {
                            LOGGER.error("Failed to create index", throwable);
                            emitter.onError(throwable);
                        } else {
                            LOGGER.info("Index created: {}", acknowledged);
                            emitter.onNext(acknowledged);
                            emitter.onCompleted();
                        }
                    });
                } else {
                    LOGGER.info("Index already exists");
                    emitter.onNext(true);
                    emitter.onCompleted();
                }
            } catch (Exception e) {
                LOGGER.error("Failed to create index", e);
                emitter.onError(e);
            } finally {
                IOUtils.closeQuietly(resourceAsStream);
            }
        }, Emitter.BackpressureMode.LATEST);
    }

    private boolean indexExists(String indexName) {
        try {
            ExistsRequest request = ExistsRequest.of(b -> b.index(indexName));
            return client.indices().exists(request).value();
        } catch (Exception e) {
            LOGGER.error("Failed to check if index exists", e);
            return false;
        }
    }

    /**
     * Bulk insert data to ES.
     *
     * @param products  Data to insert
     * @param indexName Target ES index
     * @param type      Target ES type (deprecated in ES 7.x)
     * @return Results of bulk insertion
     */
    public Observable<Boolean> index(List<ProductEntity> products, String indexName, String type) {
        List<BulkOperation> operations = new ArrayList<>();

        try {
            for (ProductEntity product : products) {
                String id = DigestUtils.sha1Hex(product.getUrl() + product.getProductName());
                JsonNode productJson = objectMapper.readTree(product.getJson());

                IndexOperation<JsonNode> indexOp = IndexOperation.of(b -> b
                        .index(indexName)
                        .id(id)
                        .document(productJson)
                );

                operations.add(BulkOperation.of(b -> b.index(indexOp)));
                LOGGER.info("Preparing to index {}/{} value {}", indexName, id, product.getUrl());
            }
        } catch (Exception e) {
            LOGGER.error("Failed to generate bulk add operation", e);
        }

        return Observable.create(emitter -> {
            try {
                esConcurrency.acquire();

                CompletableFuture.supplyAsync(() -> {
                    try {
                        BulkRequest bulkRequest = BulkRequest.of(b -> b.operations(operations));
                        return client.bulk(bulkRequest);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).whenComplete((bulkResponse, throwable) -> {
                    try {
                        if (throwable != null) {
                            LOGGER.error("Bulk indexing failed", throwable);
                            emitter.onError(throwable);
                        } else {
                            if (bulkResponse.errors()) {
                                StringBuilder failures = new StringBuilder();
                                bulkResponse.items().forEach(item -> {
                                    if (item.error() != null) {
                                        failures.append(item.error().reason()).append("; ");
                                    }
                                });
                                LOGGER.error("Failed to index products: {}", failures);
                            } else {
                                LOGGER.info("Successfully indexed {} in {}ms",
                                        bulkResponse.items().size(), bulkResponse.took());
                            }
                            emitter.onNext(!bulkResponse.errors());
                            emitter.onCompleted();
                        }
                    } finally {
                        esConcurrency.release();
                    }
                });
            } catch (InterruptedException e) {
                LOGGER.error("Failed to acquire semaphore", e);
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.BUFFER);
    }

    /**
     * Index price data for products
     *
     * @param products  Products to index price data for
     * @param indexName Target ES index
     * @param type      Target ES type (deprecated in ES 7.x)
     * @return Results of bulk insertion
     */
    public Observable<Boolean> price_index(List<ProductEntity> products, String indexName, String type) {
        List<BulkOperation> operations = new ArrayList<>();

        try {
            for (ProductEntity product : products) {
                LOGGER.info("Preparing to index {}/{} value {}", indexName, type, product.getUrl());

                ObjectNode priceDoc = objectMapper.createObjectNode();
                priceDoc.put("url", product.getUrl());
                priceDoc.put("crawlDate", Date.from(Instant.now()).getTime());
                
                String price = "N/A";
                if (product.getSpecialPrice() != null && !product.getSpecialPrice().isEmpty()) {
                    price = product.getSpecialPrice();
                } else if (product.getRegularPrice() != null && !product.getRegularPrice().isEmpty()) {
                    price = product.getRegularPrice();
                }

                if (price.equals("N/A")) {
                    LOGGER.warn("Unable to find price for product: {}", product.getUrl());
                    continue;
                }

                try {
                    priceDoc.put("price", Double.valueOf(price));
                } catch (NumberFormatException e) {
                    LOGGER.warn("Invalid price format for product {}: {}", product.getUrl(), price);
                    continue;
                }

                String id = DigestUtils.sha1Hex(product.getUrl() + product.getProductName() + price);

                IndexOperation<JsonNode> indexOp = IndexOperation.of(b -> b
                        .index(indexName)
                        .id(id)
                        .document(priceDoc)
                );

                operations.add(BulkOperation.of(b -> b.index(indexOp)));
            }
        } catch (Exception e) {
            LOGGER.error("Failed to generate bulk add operation", e);
        }

        return Observable.create(emitter -> {
            try {
                esConcurrency.acquire();

                CompletableFuture.supplyAsync(() -> {
                    try {
                        BulkRequest bulkRequest = BulkRequest.of(b -> b.operations(operations));
                        return client.bulk(bulkRequest);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).whenComplete((bulkResponse, throwable) -> {
                    try {
                        if (throwable != null) {
                            LOGGER.error("Price bulk indexing failed", throwable);
                            emitter.onError(throwable);
                        } else {
                            if (bulkResponse.errors()) {
                                StringBuilder failures = new StringBuilder();
                                bulkResponse.items().forEach(item -> {
                                    if (item.error() != null) {
                                        failures.append(item.error().reason()).append("; ");
                                    }
                                });
                                LOGGER.error("Failed to price index products: {}", failures);
                            } else {
                                LOGGER.info("Successfully price indexed {} in {}ms",
                                        bulkResponse.items().size(), bulkResponse.took());
                            }
                            emitter.onNext(!bulkResponse.errors());
                            emitter.onCompleted();
                        }
                    } finally {
                        esConcurrency.release();
                    }
                });
            } catch (InterruptedException e) {
                LOGGER.error("Failed to acquire semaphore", e);
                emitter.onError(e);
            }
        }, Emitter.BackpressureMode.BUFFER);
    }
}