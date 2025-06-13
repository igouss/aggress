package com.naxsoft.service;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service interface for caching and temporary storage operations.
 * Provides high-level caching functionality abstracted from Redis implementation details.
 */
@Service
public interface CacheService {

    /**
     * Mark a web page as successfully parsed.
     *
     * @param webPageEntity The web page entity to mark as parsed
     * @return Mono with count of items moved
     */
    Mono<Long> markWebPageAsParsed(WebPageEntity webPageEntity);

    /**
     * Add a product page entry to the processing queue.
     *
     * @param productEntity The product entity to queue
     * @return Mono with count of items added
     */
    Mono<Long> addProductPageEntry(ProductEntity productEntity);

    /**
     * Add a web page entry to the crawling queue.
     *
     * @param webPageEntity The web page entity to queue
     * @return Mono with count of items added
     */
    Mono<Long> addWebPageEntry(WebPageEntity webPageEntity);

    /**
     * Get all products from the cache.
     *
     * @return Flux stream of product entities
     */
    Flux<ProductEntity> getProducts();

    /**
     * Get count of unparsed pages by type.
     *
     * @param type The page type to count
     * @return Mono with count of unparsed pages
     */
    Mono<Long> getUnparsedCount(String type);

    /**
     * Get unparsed web pages by type with a limit.
     *
     * @param type  Page type to retrieve
     * @param count Maximum number of pages to retrieve
     * @return Flux stream of web page entities
     */
    Flux<WebPageEntity> getUnparsedByType(String type, Long count);

    /**
     * Clean up specified cache tables.
     *
     * @param tables Array of table names to clean
     * @return Mono indicating cleanup completion
     */
    Mono<String> cleanUp(String[] tables);

    /**
     * Check if cache service is available and healthy.
     *
     * @return true if service is available
     */
    boolean isHealthy();
}