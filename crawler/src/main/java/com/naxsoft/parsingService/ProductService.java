package com.naxsoft.parsingService;

import com.naxsoft.common.entity.ProductEntity;
import com.naxsoft.storage.Persistent;

import java.util.List;

public class ProductService {
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
