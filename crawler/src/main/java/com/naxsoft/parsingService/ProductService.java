package com.naxsoft.parsingService;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.storage.Persistent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProductService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private final Persistent database;

    public ProductService(Persistent database) {
        this.database = database;
    }

    public Long save(ProductEntity products) {
        return database.addProductPageEntry(products);
    }

    /**
     * Get stream of unindexed products
     *
     * @return Stream of unindexed products
     */
    public List<ProductEntity> getProducts() {
        return database.getProducts();
    }

    /**
     * Mark all products as indexed
     */
    public void markAllAsIndexed() {
        database.markAllProductPagesAsIndexed();
    }
}
