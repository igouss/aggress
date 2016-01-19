//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.entity.ProductEntity;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collection;

/**
 *
 */
public class ProductService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ProductService.class);
    private final Database database;

    /**
     * @param database
     */
    public ProductService(Database database) {
        this.database = database;
    }

    /**
     * @param products Save
     */
    public void save(Collection<ProductEntity> products) {
        database.executeTransaction(session -> {
            for (ProductEntity productEntity : products) {
                session.insert(productEntity);
            }
            return true;
        });
    }

    /**
     * Get stream of unindexed products
     * @return Stream of unindexed products
     */
    public Observable<ProductEntity> getProducts() {
        String queryString = "from ProductEntity where indexed=false";
        return database.scroll(queryString);
    }

    /**
     * Mark all products as indexed
     */
    public void markAllAsIndexed() {
        int rc = database.executeTransaction(session -> {
            Query query = session.createQuery("update ProductEntity as p set p.indexed = true");
            return query.executeUpdate();
        });
        LOGGER.info("The number of entities affected: {}", rc);
    }
}
