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
import rx.observables.AbstractOnSubscribe;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class WebPageService<T> {
    private final Logger logger;
    private Database database;

    public WebPageService(Database database) {
        this.database = database;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void save(Set<WebPageEntity> webPageEntitySet) {
        Session session = this.database.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        int i = 0;
        Iterator var6 = webPageEntitySet.iterator();

        while (var6.hasNext()) {
            WebPageEntity webPageEntity = (WebPageEntity) var6.next();
            session.save(webPageEntity);
            ++i;
            if (i % 20 == 0) {
                session.flush();
                session.clear();
            }
        }

        session.flush();
        session.clear();
        tx.commit();
        session.close();
    }

    public void markParsed(Collection<WebPageEntity> parsedProductList) {
        Session session = this.database.getSessionFactory().openSession();
        Query query = session.createQuery("update WebPageEntity set parsed = true where id = :id");
        Transaction tx = session.beginTransaction();
        int count = 0;

        for (WebPageEntity webPageEntity : parsedProductList) {
            query.setInteger("id", webPageEntity.getId()).executeUpdate();
            ++count;
            if (count % 20 == 0) {
                session.flush();
                session.clear();
            }
        }

        session.flush();
        session.clear();
        tx.commit();
        session.close();
    }

    private IterableListScrollableResults get(String queryString) {
        Session session = this.database.getSessionFactory().openSession();
        Query query = session.createQuery(queryString);
        query.setCacheable(false);
        query.setReadOnly(true);
        ScrollableResults result = query.scroll(ScrollMode.FORWARD_ONLY);
        IterableListScrollableResults webPageEntities = new IterableListScrollableResults(session, result);
        return webPageEntities;
    }

    private Observable<WebPageEntity> getAsync(String queryString) {
        return Observable.<WebPageEntity>create(o -> {
            try {
                Session session = this.database.getSessionFactory().openSession();
                Query query = session.createQuery(queryString);
                query.setCacheable(false);
                query.setReadOnly(true);
                ScrollableResults result = query.scroll(ScrollMode.FORWARD_ONLY);
                while (result.next()) {
                    o.onNext((WebPageEntity) result.get(0));
                }
                o.onCompleted();
                result.close();
                session.close();
            } catch (Exception e) {
                logger.error("Failed to get process async hibernate query " + queryString, e);
                o.onError(e);
            }
        });
    }


    private Observable<WebPageEntity> getAsync2(String queryString) {
        return Observable.create(AbstractOnSubscribe.<WebPageEntity, ScrollableResults>create(s -> {
            try {
                ScrollableResults state = s.state();
                while (state.next()) {
                    s.onNext((WebPageEntity) state.get(0));
                }
                s.onCompleted();
            } catch (Exception e) {
                s.onError(e);
            }
        }, s -> {
            Session session = this.database.getSessionFactory().openSession();
            Query query = session.createQuery(queryString);
            query.setCacheable(false);
            query.setReadOnly(true);
            ScrollableResults result = query.scroll(ScrollMode.FORWARD_ONLY);
            return result;
        }, scrollableResults -> {
            scrollableResults.close();
        }));
    }

    public IterableListScrollableResults<T> getUnparsedProductList() {
        String queryString = "from WebPageEntity where type = \'productList\' and parsed = false order by rand()";
        return this.get(queryString);
    }

    public IterableListScrollableResults<T> getUnparsedProductPage() {
        String queryString = "from WebPageEntity where type = \'productPage\' and parsed = false order by rand()";
        return this.get(queryString);
    }

    public IterableListScrollableResults<T> getUnparsedProductPageRaw() {
        String queryString = "from WebPageEntity where type = \'productPageRaw\' and parsed = false order by rand()";
        return this.get(queryString);
    }

    public IterableListScrollableResults<T> getUnparsedFrontPage() {
        String queryString = "from WebPageEntity where type = \'frontPage\' and parsed = false order by rand()";
        return this.get(queryString);
    }

    public IterableListScrollableResults<T> getUnparsedPage() {
        String queryString = "from WebPageEntity where parsed = false order by rand()";
        return this.get(queryString);
    }

    public Observable<WebPageEntity> getUnparsedProductListAsync() {
        String queryString = "from WebPageEntity where type = \'productList\' and parsed = false order by rand()";
        return this.getAsync(queryString);
    }

    public Observable<WebPageEntity> getUnparsedProductPageAsync() {
        String queryString = "from WebPageEntity where type = \'productPage\' and parsed = false order by rand()";
        return this.getAsync(queryString);
    }

    public Observable<WebPageEntity> getUnparsedProductPageRawAsync() {
        String queryString = "from WebPageEntity where type = \'productPageRaw\' and parsed = false order by rand()";
        return this.getAsync2(queryString);
    }

    public Observable<WebPageEntity> getUnparsedFrontPageAsync() {
        String queryString = "from WebPageEntity where type = \'frontPage\' and parsed = false order by rand()";
        return this.getAsync(queryString);
    }

    public Observable<WebPageEntity> getUnparsedPageAsync() {
        String queryString = "from WebPageEntity where parsed = false order by rand()";
        return this.getAsync(queryString);
    }
}
