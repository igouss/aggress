package com.naxsoft.service;

import com.naxsoft.entity.ProductEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Service interface for product search operations.
 * Provides high-level search functionality abstracted from Elasticsearch implementation details.
 */
@Service
public interface SearchService {

    /**
     * Create product search index if it doesn't exist.
     *
     * @param indexName Name of the index to create
     * @param type      Document type (deprecated in ES 7.x but kept for compatibility)
     * @return Mono indicating success or failure
     */
    Mono<Boolean> createProductIndex(String indexName, String type);

    /**
     * Index a batch of products for search.
     *
     * @param products  List of products to index
     * @param indexName Target index name
     * @param type      Document type
     * @return Mono indicating indexing success
     */
    Mono<Boolean> indexProducts(List<ProductEntity> products, String indexName, String type);

    /**
     * Index price data for products (separate index for price tracking).
     *
     * @param products  List of products with price data
     * @param indexName Target price index name
     * @param type      Document type
     * @return Mono indicating indexing success
     */
    Mono<Boolean> indexProductPrices(List<ProductEntity> products, String indexName, String type);

    /**
     * Check if search service is available and healthy.
     *
     * @return true if service is available
     */
    boolean isHealthy();
}