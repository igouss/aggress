//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.SourceEntity;
import org.hibernate.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collection;
import java.util.Set;

public class ProductService {
    private final Logger logger;
    private Elastic elastic;
    private Database database;

    public ProductService(Elastic elastic, Database database) {
        this.elastic = elastic;
        this.database = database;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void save(Collection<ProductEntity> products) {
        Session session = this.database.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            int i = 0;
            for (ProductEntity productEntity : products) {
                session.save(productEntity);
                if ((++i % 20) == 0) {
                    session.flush();
                }
            }
            session.flush();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.commit();
            }
        } finally {
            session.close();
        }
    }

    public Observable<ProductEntity> getProducts() {
        String queryString = "from ProductEntity where indexed=false";
        return Observable.defer(() -> new ObservableQuery<ProductEntity>(database).execute(queryString));
    }

    public void markAllAsIndexed() {
        Session session = this.database.getSessionFactory().openSession();
        Query query = session.createQuery("update ProductEntity as p set p.indexed = true");
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            query.executeUpdate();
            session.flush();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                session.flush();
                tx.commit();
            }
        } finally {
            session.close();
        }
    }


}
