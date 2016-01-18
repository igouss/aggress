//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.entity.ProductEntity;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.StatelessSession;
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
     *
     * @param database
     */
    public ProductService(Database database) {
        this.database = database;
    }

    /**
     *
     * @param products
     */
    public void save(Collection<ProductEntity> products) {
        StatelessSession session = null;
        org.hibernate.Transaction tx = null;
        try {
            session = database.getSessionFactory().openStatelessSession();
            tx = session.beginTransaction();
            for (ProductEntity productEntity : products) {
                session.insert(productEntity);
            }
            tx.commit();
        } catch (HibernateException e) {
            LOGGER.error("Failed to save products", e);
            if (null != tx) {
                tx.rollback();
            }
        } finally {
            if (null != session) {
                session.close();
            }
        }

    }

    /**
     *
     * @return
     */
    public Observable<ProductEntity> getProducts() {
        String queryString = "from ProductEntity where indexed=false";
        return new ObservableQuery<ProductEntity>(database).execute(queryString);
    }

    /**
     *
     */
    public void markAllAsIndexed() {
        StatelessSession session = null;
        org.hibernate.Transaction tx = null;

        try {
            session = database.getSessionFactory().openStatelessSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("update ProductEntity as p set p.indexed = true");

            int rc = query.executeUpdate();
            LOGGER.info("The number of entities affected: {}", rc);
            tx.commit();
        } catch (HibernateException e) {
            LOGGER.error("Failed to mark all as indexed", e);
            if (null != tx) {
                tx.rollback();
            }
        } finally {
            if (null != session) {
                session.close();
            }
        }
    }
}
