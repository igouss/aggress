package com.naxsoft.parsingService;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.storage.Persistent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 *
 */
public class ProductService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private final Persistent database;

    /**
     * @param database
     */
    public ProductService(Persistent database) {
        this.database = database;
    }

    /**
     * @param products Save
     */
    public Observable<Long> save(Observable<ProductEntity> products) {
        return database.addProductPageEntry(products);
    }

    /**
     * Get stream of unindexed products
     *
     * @return Stream of unindexed products
     */
    public Observable<ProductEntity> getProducts() {
        return database.getProducts();
    }

    /**
     * Mark all products as indexed
     */
    public void markAllAsIndexed() {
        database.markAllProductPagesAsIndexed().subscribe(
                value -> LOGGER.info("The number of entities affected: {}", value),
                err -> LOGGER.error("Failed to mark as indexed", err),
                () -> LOGGER.info("markAllProductPagesAsIndexed complete"));
    }
}
