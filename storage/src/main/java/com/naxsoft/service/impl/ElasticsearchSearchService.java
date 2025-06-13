package com.naxsoft.service.impl;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.service.SearchService;
import com.naxsoft.storage.elasticsearch.Elastic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Elasticsearch implementation of the SearchService interface.
 * Delegates to the Elastic component for actual Elasticsearch operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticsearchSearchService implements SearchService {

    private final Elastic elasticsearchClient;

    @Override
    public Mono<Boolean> createProductIndex(String indexName, String type) {
        log.debug("Creating product index: {} with type: {}", indexName, type);
        return elasticsearchClient.createIndex(indexName, type);
    }

    @Override
    public Mono<Boolean> indexProducts(List<ProductEntity> products, String indexName, String type) {
        log.debug("Indexing {} products to index: {}", products.size(), indexName);
        return elasticsearchClient.index(products, indexName, type);
    }

    @Override
    public Mono<Boolean> indexProductPrices(List<ProductEntity> products, String indexName, String type) {
        log.debug("Indexing prices for {} products to index: {}", products.size(), indexName);
        return elasticsearchClient.price_index(products, indexName, type);
    }

    @Override
    public boolean isHealthy() {
        try {
            // Simple health check - could be enhanced with actual cluster health query
            return elasticsearchClient != null;
        } catch (Exception e) {
            log.warn("Elasticsearch health check failed", e);
            return false;
        }
    }
}