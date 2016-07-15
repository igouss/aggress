package com.naxsoft.database;

import com.naxsoft.entity.ProductEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collection;

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
    public void save(Collection<ProductEntity> products) {
        for (ProductEntity productEntity : products) {
            database.save(productEntity);
        }
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
        database.markAllProductPagesAsIndexed().subscribe(value -> {
                    LOGGER.info("The number of entities affected: {}", value);
                },
                err -> LOGGER.error("Failed to mark as indexed", err),
                () -> {
                    LOGGER.info("markAllProductPagesAsIndexed complete");
                });
    }
}
