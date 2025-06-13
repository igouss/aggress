package com.naxsoft.storage;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Persistent extends AutoCloseable, Cloneable {
    /**
     * Close connection to the underling database
     */
    void close();

    /**
     * Get number of unparsed WebPageEntries
     * @param type Type of WebPageEntries
     * @return Number unparsed of WebPageEntries of specified type
     */
    Mono<Long> getUnparsedCount(String type);

    /**
     * Mark webPageEntity as parsed
     *
     * @param webPageEntity page to make as parsed
     * @return Number of entries affected. Should be 1 on success.
     */
    Mono<Long> markWebPageAsParsed(WebPageEntity webPageEntity);

    /**
     * Mark all ProductEntity as parsed
     *
     * @return number of ProductEntity's affected
     */
    Mono<Integer> markAllProductPagesAsIndexed();

    /**
     * Persist ProductEntity
     *
     * @param productEntity entity to persist
     * @return True of success, false otherwise
     */
    Mono<Long> addProductPageEntry(ProductEntity productEntity);

    /**
     * Persist WebPageEntity
     *
     * @param webPageEntity entity to persist
     * @return True of success, false otherwise
     */
    Mono<Long> addWebPageEntry(WebPageEntity webPageEntity);

    /**
     * Get all ProductEntity from the storage
     *
     * @return all ProductEntity from the storage
     */
    Flux<ProductEntity> getProducts();

    /**
     * Get at most count unparsed WebPageEntity's of specified type
     *
     * @param type Specify type of unparsed WebPageEntity's to return
     * @return all unparsed WebPageEntity's of specified type
     */
    Flux<WebPageEntity> getUnparsedByType(String type, Long count);

    /**
     * Delete data from tables
     *
     * @param tables WebPageEntity or ProductEntity
     */
    Mono<String> cleanUp(String[] tables);
}
