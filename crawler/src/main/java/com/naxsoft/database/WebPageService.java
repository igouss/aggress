//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.entity.WebPageEntity;
import org.hibernate.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class WebPageService {
    private final Logger logger;
    private Database database;

    public WebPageService(Database database) {
        this.database = database;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void save(Set<WebPageEntity> webPageEntitySet) {
        Session session = this.database.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            int i = 0;
            Iterator var6 = webPageEntitySet.iterator();

            while (var6.hasNext()) {
                WebPageEntity webPageEntity = (WebPageEntity) var6.next();
                logger.debug("Saving " + webPageEntity);
                session.save(webPageEntity);
                ++i;
                if (i % 20 == 0) {
                    session.flush();
                }
            }
            session.flush();
            tx.commit();
        } catch (Exception e) {
            if (tx != null) {
                session.flush();
                tx.commit();
            }
        } finally {
            session.close();
        }
    }

    public void markParsed(Collection<WebPageEntity> parsedProductList) {
        Session session = this.database.getSessionFactory().openSession();
        Query query = session.createQuery("update WebPageEntity set parsed = true where id = :id");
        Transaction tx = null;

        try {
            tx = session.beginTransaction();
            int count = 0;
            for (WebPageEntity webPageEntity : parsedProductList) {
                logger.debug("update WebPageEntity set parsed = true where id = " + webPageEntity.getId());
                query.setInteger("id", webPageEntity.getId()).executeUpdate();
                if (++count % 20 == 0) {
                    session.flush();
                }
            }
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

    private IterableListScrollableResults get(String queryString) {
        StatelessSession session = this.database.getSessionFactory().openStatelessSession();
        Query query = session.createQuery(queryString);
        query.setCacheable(false);
        query.setReadOnly(true);
        query.setFetchSize(128);
        ScrollableResults result = query.scroll(ScrollMode.FORWARD_ONLY);
        return new IterableListScrollableResults(session, result);
    }



    public IterableListScrollableResults<WebPageEntity> getUnparsedProductList() {
        String queryString = "from WebPageEntity where type = \'productList\' and parsed = false order by rand()";
        return this.get(queryString);
    }

    public IterableListScrollableResults<WebPageEntity> getUnparsedProductPage() {
        String queryString = "from WebPageEntity where type = \'productPage\' and parsed = false order by rand()";
        return this.get(queryString);
    }

    public IterableListScrollableResults<WebPageEntity> getUnparsedProductPageRaw() {
        String queryString = "from WebPageEntity where type = \'productPageRaw\' and parsed = false order by rand()";
        return this.get(queryString);
    }

    public IterableListScrollableResults<WebPageEntity> getParsedProductPageRaw() {
        String queryString = "from WebPageEntity where type = \'productPageRaw\' and parsed = false order by rand()";
        return this.get(queryString);
    }

    public IterableListScrollableResults<WebPageEntity> getUnparsedFrontPage() {
        String queryString = "from WebPageEntity where type = \'frontPage\' and parsed = false order by rand()";
        return this.get(queryString);
    }

    public void deDup() {
        Session session = null;
        try {
            session = database.getSessionFactory().openSession();
            SQLQuery sqlQuery = session.createSQLQuery("DELETE FROM guns.web_page USING guns.web_page wp2 WHERE guns.web_page.url = wp2.url AND guns.web_page.type = wp2.type AND guns.web_page.id < wp2.id");
            sqlQuery.executeUpdate();
        } finally {
            if (null != session) {
                session.close();
            }
        }
    }

//    private Observable<WebPageEntity> getAsync(String queryString) {
//        return Observable.<WebPageEntity>create(o -> {
//            try {
//                Session session = this.database.getSessionFactory().openSession();
//                Query query = session.createQuery(queryString);
//                query.setCacheable(false);
//                query.setReadOnly(true);
//                ScrollableResults result = query.scroll(ScrollMode.FORWARD_ONLY);
//                while (result.next()) {
//                    o.onNext((WebPageEntity) result.get(0));
//                }
//                o.onCompleted();
//                result.close();
//                session.close();
//            } catch (Exception e) {
//                logger.error("Failed to get process async hibernate query " + queryString, e);
//                o.onError(e);
//            }
//        });
//    }
//
//
//    public Observable<WebPageEntity> getAsync2(String queryString) {
//        return Observable.using(
//                () -> this.database.getSessionFactory().openSession()
//                , session -> Observable.using(() -> {
//                            Query query = session.createQuery(queryString);
//                            query.setCacheable(false);
//                            query.setReadOnly(true);
//                            return query.scroll(ScrollMode.FORWARD_ONLY);
//                        }, scrollableResults -> Observable.from(new IterableListScrollableResults<>(session, scrollableResults)), scrollableResults -> scrollableResults.close()
//                ), session -> session.close());
//    }

//    public IterableListScrollableResults<WebPageEntity> getUnparsedPage() {
//        String queryString = "from WebPageEntity where parsed = false order by rand()";
//        return this.get(queryString);
//    }
//
//    public Observable<WebPageEntity> getUnparsedProductListAsync() {
//        String queryString = "from WebPageEntity where type = \'productList\' and parsed = false order by rand()";
//        return this.getAsync2(queryString);
//    }
//
//    public Observable<WebPageEntity> getUnparsedProductPageAsync() {
//        String queryString = "from WebPageEntity where type = \'productPage\' and parsed = false order by rand()";
//        return this.getAsync(queryString);
//    }
//
//    public Observable<WebPageEntity> getUnparsedProductPageRawAsync() {
//        String queryString = "from WebPageEntity where type = \'productPageRaw\' and parsed = false order by rand()";
//        return this.getAsync2(queryString);
//    }
//
//    public Observable<WebPageEntity> getUnparsedFrontPageAsync() {
//        String queryString = "from WebPageEntity where type = \'frontPage\' and parsed = false order by rand()";
//        return this.getAsync2(queryString);
//    }
//
//    public Observable<WebPageEntity> getUnparsedPageAsync() {
//        String queryString = "from WebPageEntity where parsed = false order by rand()";
//        return this.getAsync2(queryString);
//    }
}
