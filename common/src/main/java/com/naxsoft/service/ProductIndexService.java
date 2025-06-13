package com.naxsoft.service;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.service.dto.IndexResult;
import com.naxsoft.service.dto.IndexStats;
import com.naxsoft.service.dto.SearchQuery;
import com.naxsoft.service.dto.SearchResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Modern service interface for product indexing and search operations.
 * <p>
 * This interface provides:
 * - CompletableFuture-based async operations
 * - Type-safe request/response objects
 * - Clean separation from storage implementation details
 * - Easy testing and mocking capabilities
 * <p>
 * Replaces direct Elasticsearch client usage with clean abstraction.
 */
public interface ProductIndexService {

    /**
     * Index a single product in the search engine.
     *
     * @param product Product to index
     * @return Future containing indexing result
     */
    CompletableFuture<IndexResult> indexProduct(ProductEntity product);

    /**
     * Index multiple products in bulk for efficiency.
     *
     * @param products List of products to index
     * @return Future containing bulk indexing result
     */
    CompletableFuture<IndexResult> indexProducts(List<ProductEntity> products);

    /**
     * Search for products using structured query.
     *
     * @param query Search query with filters and pagination
     * @return Future containing search results
     */
    CompletableFuture<SearchResult<ProductEntity>> searchProducts(SearchQuery query);

    /**
     * Delete a product from the search index.
     *
     * @param productId Product ID to delete
     * @return Future containing deletion success status
     */
    CompletableFuture<Boolean> deleteProduct(String productId);

    /**
     * Delete multiple products from the search index.
     *
     * @param productIds List of product IDs to delete
     * @return Future containing deletion result
     */
    CompletableFuture<IndexResult> deleteProducts(List<String> productIds);

    /**
     * Get a product by its ID.
     *
     * @param productId Product ID to retrieve
     * @return Future containing the product, or empty if not found
     */
    CompletableFuture<ProductEntity> getProductById(String productId);

    /**
     * Check if the search index exists and is healthy.
     *
     * @return Future containing index health status
     */
    CompletableFuture<Boolean> isIndexHealthy();

    /**
     * Create the search index with proper mapping.
     *
     * @return Future containing index creation success status
     */
    CompletableFuture<Boolean> createIndex();

    /**
     * Delete the entire search index (use with caution).
     *
     * @return Future containing index deletion success status
     */
    CompletableFuture<Boolean> deleteIndex();

    /**
     * Get index statistics and metrics.
     *
     * @return Future containing index statistics
     */
    CompletableFuture<IndexStats> getIndexStats();

    /**
     * Refresh the search index to make recent changes visible.
     *
     * @return Future that completes when refresh is done
     */
    CompletableFuture<Void> refreshIndex();
}