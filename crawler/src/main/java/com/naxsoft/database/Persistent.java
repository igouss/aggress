package com.naxsoft.database;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import rx.Observable;

public interface Persistent extends AutoCloseable, Cloneable {
    /**
     * Close connection to the underling database
     */
    void close();

    /**
     * Get number of unparsed WebPageEntries
     *
     * @param type Type of WebPageEntries
     * @return Number unparsed of WebPageEntries of specified type
     */
    Observable<Long> getUnparsedCount(String type);

    /**
     * Mark webPageEntity as parsed
     *
     * @param webPageEntity page to make as parsed
     * @return Number of entries affected. Should be 1 on success.
     */
    Observable<? extends Number> markWebPageAsParsed(WebPageEntity webPageEntity);

    /**
     * Mark all ProductEntity as parsed
     *
     * @return number of ProductEntity's affected
     */
    Observable<Integer> markAllProductPagesAsIndexed();

    /**
     * Persist ProductEntity
     *
     * @param productEntity entity to persist
     * @return True of success, false otherwise
     */
    Observable<Long> save(ProductEntity productEntity);

    /**
     * Persist WebPageEntity
     *
     * @param webPageEntity entity to persist
     * @return True of success, false otherwise
     */
    Observable<Long> save(WebPageEntity webPageEntity);

    /**
     * Get all ProductEntity from the storage
     *
     * @return all ProductEntity from the storage
     */
    Observable<ProductEntity> getProducts();

    /**
     * Get at most count unparsed WebPageEntity's of specified type
     *
     * @param type Specify type of unparsed WebPageEntity's to return
     * @return all unparsed WebPageEntity's of specified type
     */
    Observable<WebPageEntity> getUnparsedByType(String type, Long count);

    /**
     * Delete data from tables
     * @param tables WebPageEntity or ProductEntity
     */
    Observable<Long> cleanUp(String[] tables);
}
