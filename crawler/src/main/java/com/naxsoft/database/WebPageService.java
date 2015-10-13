//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.entity.WebPageEntity;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;

import java.util.Collection;

public class WebPageService {
    private final Logger logger;
    private Database database;
    private ObservableQuery<WebPageEntity> observableQuery;

    public WebPageService(Database database) {
        this.database = database;
        this.logger = LoggerFactory.getLogger(this.getClass());
        observableQuery = new ObservableQuery<>(database);
    }

    private static Session getSession(Database database) {
        return database.getSessionFactory().openSession();
    }

    public void save(Collection<WebPageEntity> webPageEntitySet) {
        new Transaction<Boolean>().executeInTransaction(database, session -> {
            int i = 0;
            for (WebPageEntity webPageEntity : webPageEntitySet) {
                logger.debug("Saving " + webPageEntity);
                session.save(webPageEntity);
                if (++i % 20 == 0) {
                    session.flush();
                }
            }
            return true;
        });
    }

    public void markParsed(WebPageEntity webPageEntity) {
        new Transaction<Integer>().executeInTransaction(database, session -> {
            Query query = session.createQuery("update WebPageEntity set parsed = true where id = :id");
            logger.debug("update WebPageEntity set parsed = true where id = " + webPageEntity.getId());
            return query.setInteger("id", webPageEntity.getId()).executeUpdate();
        });
    }

    /**
     * Keep producing unparsedCount till it is equals to 0
     *
     * @param type Column type to check against
     * @return row count in type
     */
    public Observable<Long> getUnparsedCount(String type) {
        return Observable.create(subscriber -> {
            Long rc = 0L;
            Transaction<Long> transaction = new Transaction<>();
            do {
                rc = transaction.executeInTransaction(database, session -> {
                    String queryString = "select count (id) from WebPageEntity as w where w.parsed = false and w.type = :type";
                    Query query = session.createQuery(queryString);
                    query.setString("type", type);
                    Long count = (Long) query.list().get(0);
                    if (0 != count) {
                        subscriber.onNext(count);
                    } else {
                        subscriber.onCompleted();
                    }
                    return count;
                });
            } while (rc != 0L && !subscriber.isUnsubscribed());
            subscriber.onCompleted();
        });
    }

    public void deDup() {
        final String queryString = "DELETE FROM guns.web_page USING guns.web_page wp2 WHERE guns.web_page.url = wp2.url AND guns.web_page.type = wp2.type AND guns.web_page.id < wp2.id";
        new Transaction<Integer>().executeInTransaction(database, session -> {
            SQLQuery sqlQuery = session.createSQLQuery(queryString);
            int result = sqlQuery.executeUpdate();
            logger.info("De-dupped " + result + " rows");
            return result;
        });
    }

    public Observable<WebPageEntity> getUnparsedProductList() {
        final String queryString = "from WebPageEntity where type = 'productList' and parsed = false order by rand()";
        return executeQuery(queryString, "productList");
    }

    public Observable<WebPageEntity> getUnparsedProductPage() {
        final String queryString = "from WebPageEntity where type = 'productPage' and parsed = false order by rand()";
        return executeQuery(queryString, "productPage");
    }

    public Observable<WebPageEntity> getUnparsedProductPageRaw() {
        final String queryString = "from WebPageEntity where type = 'productPageRaw' order by rand()";
        return executeQuery(queryString, "productPageRaw");
    }

    public Observable<WebPageEntity> getUnparsedFrontPage() {
        final String queryString = "from WebPageEntity where type = 'frontPage' and parsed = false order by rand()";
        return executeQuery(queryString, "frontPage");
    }

    private Observable<WebPageEntity> executeQuery(String query, String condColumn) {
        return Observable.create(subscriber -> {
            getUnparsedCount(condColumn).subscribe(unparsedCount -> {
                if (unparsedCount != 0) {
                    observableQuery.execute(query).subscribe(subscriber::onNext);
                } else {
                    subscriber.onCompleted();
                }
            });
        });
    }

    class Transaction<R> {
        public R executeInTransaction(Database database, Func1<Session, R> action) {
            Session session = getSession(database);
            org.hibernate.Transaction tx = null;
            try {
                tx = session.beginTransaction();
                R result = action.call(session);
                session.flush();
                tx.commit();
                return result;
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
    }
}
