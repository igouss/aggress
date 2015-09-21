//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.entity.WebPageEntity;
import org.hibernate.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Action1;

import java.util.Collection;

public class WebPageService {
    private final Logger logger;
    private Database database;

    public WebPageService(Database database) {
        this.database = database;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    private static Session getSession(Database database) {
        return database.getSessionFactory().openSession();
    }


    public void executeInTransaction(Action1<Session> action1) {
        Session session = getSession(this.database);
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            action1.call(session);
            session.flush();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                session.flush();
                tx.commit();
            }
            throw e;
        } finally {
            session.close();
        }

    }

    public void save(Collection<WebPageEntity> webPageEntitySet) {
        executeInTransaction(session -> {
            int i = 0;
            for (WebPageEntity webPageEntity : webPageEntitySet) {
                logger.debug("Saving " + webPageEntity);
                session.save(webPageEntity);
                if (++i % 20 == 0) {
                    session.flush();
                }
            }
        });
    }

    public void markParsed(WebPageEntity webPageEntity) {

            executeInTransaction(session -> {
                Query query = session.createQuery("update WebPageEntity set parsed = true where id = :id");
                logger.debug("update WebPageEntity set parsed = true where id = " + webPageEntity.getId());
                query.setInteger("id", webPageEntity.getId()).executeUpdate();
            });

    }

    public long getUnparsedCount(String type) {
        Session session = getSession(this.database);
        String queryString = "select count (id) from WebPageEntity as w where w.parsed = false and w.type = :type";
        Query query = session.createQuery(queryString);
        Transaction tx = null;
        long count = 0;
        try {
            tx = session.beginTransaction();
            query.setString("type", type);
            count = (Long) query.list().get(0);
            session.flush();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                session.flush();
                tx.commit();
            }
            throw e;
        } finally {
            session.close();
        }
        return count;
    }

    public Observable<WebPageEntity> getUnparsedProductList() {
        final String queryString = "from WebPageEntity where type = 'productList' and parsed = false order by rand()";
        return Observable.defer(() -> Observable.create(subscriber -> {
            do {
                Observable<WebPageEntity> result = new ObservableQuery<WebPageEntity>(database).execute(queryString);
                result.subscribe(webPageEntity -> subscriber.onNext(webPageEntity));
            } while (getUnparsedCount("productList") > 0);
            subscriber.onCompleted();
        }));
    }

    public Observable<WebPageEntity> getUnparsedProductPage() {
        final String queryString = "from WebPageEntity where type = 'productPage' and parsed = false order by rand()";
        return Observable.defer(() -> Observable.create(subscriber -> {
            do {
                Observable<WebPageEntity> result = new ObservableQuery<WebPageEntity>(database).execute(queryString);
                result.subscribe(webPageEntity -> subscriber.onNext(webPageEntity));
            } while (getUnparsedCount("productPage") > 0);
            subscriber.onCompleted();
        }));
    }

    public Observable<WebPageEntity> getUnparsedProductPageRaw() {
        final String queryString = "from WebPageEntity where type = 'productPageRaw' and parsed = false order by rand()";
        return Observable.defer(() -> Observable.create(subscriber -> {
            do {
                Observable<WebPageEntity> result = new ObservableQuery<WebPageEntity>(database).execute(queryString);
                result.subscribe(webPageEntity -> subscriber.onNext(webPageEntity));
            } while (getUnparsedCount("productPageRaw") > 0);
            subscriber.onCompleted();
        }));
    }

    public Observable<WebPageEntity> getUnparsedFrontPage() {
        final String queryString = "from WebPageEntity where type = 'frontPage' and parsed = false order by rand()";
        return Observable.defer(() -> Observable.create(subscriber -> {
           do {
               Observable<WebPageEntity> result = new ObservableQuery<WebPageEntity>(database).execute(queryString);
               result.subscribe(webPageEntity -> subscriber.onNext(webPageEntity));
           } while (getUnparsedCount("frontPage") > 0);
            subscriber.onCompleted();
        }));
    }

    public void deDup() {
        final String queryString = "DELETE FROM guns.web_page USING guns.web_page wp2 WHERE guns.web_page.url = wp2.url AND guns.web_page.type = wp2.type AND guns.web_page.id < wp2.id";
        executeInTransaction(session -> {
            SQLQuery sqlQuery = session.createSQLQuery(queryString);
            int result = sqlQuery.executeUpdate();
            logger.info("De-dupped " + result + " rows");
        });
    }
}
