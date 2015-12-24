//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.entity.ProductEntity;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collection;

public class ProductService {
    private final static Logger logger = LoggerFactory.getLogger(ProductService.class);
    private final Database database;

    public ProductService(Database database) {
        this.database = database;
    }

    public void save(Collection<ProductEntity> products) {
        Session session = null;
        Transaction tx = null;
        try {
            session = database.getSessionFactory().openSession();
            tx = session.beginTransaction();
            int i = 0;
            for (ProductEntity productEntity : products) {
                session.save(productEntity);
                if (0 == (++i % 20)) {
                    session.flush();
                }
            }
            session.flush();
            tx.commit();
        } catch (HibernateException e) {
            logger.error("Failed to save products", e);
            if (null != tx) {
                tx.rollback();
            }
        } finally {
            if (null != session) {
                session.close();
            }
        }

    }

    public Observable<ProductEntity> getProducts() {
        String queryString = "from ProductEntity where indexed=false";
        return new ObservableQuery<ProductEntity>(database).execute(queryString);
    }

    public void markAllAsIndexed() {
        Session session = null;
        Transaction tx = null;

        try {
            session = database.getSessionFactory().openSession();
            tx = session.beginTransaction();
            Query query = session.createQuery("update ProductEntity as p set p.indexed = true");

            int rc = query.executeUpdate();
            logger.info("The number of entities affected: {}", rc);
            session.flush();
            tx.commit();
        } catch (HibernateException e) {
            logger.error("Failed to mark all as indexed", e);
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
