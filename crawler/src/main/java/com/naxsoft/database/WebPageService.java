package com.naxsoft.database;

import com.naxsoft.entity.SourceEntity;
import com.naxsoft.entity.WebPageEntity;
import org.hibernate.*;

import java.util.Iterator;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class WebPageService {
    private Database database;

    public WebPageService(Database database) {
        this.database = database;
    }

    public void save(SourceEntity source, Set<WebPageEntity> webPageEntitySet) {
        Session session = database.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        int i = 0;
        for (WebPageEntity webPageEntity : webPageEntitySet) {
            webPageEntity.setSourceBySourceId(source);
            session.save(webPageEntity);
            if (++i % 20 == 0) { //20, same as the JDBC batch size
                //flush a batch of inserts and release memory:
                session.flush();
                session.clear();
            }

        }
        session.flush();
        tx.commit();
        session.close();
    }

    public void markParsed(Set<WebPageEntity> parsedProductList) {
        Session session = database.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        int count = 0;
        for (WebPageEntity webPageEntity : parsedProductList) {
            webPageEntity.setParsed(true);
            session.update(webPageEntity);
            if (++count % 20 == 0) {
                //flush a batch of updates and release memory:
                session.flush();
                session.clear();
            }
        }
        session.flush();
        tx.commit();
        session.close();

    }

    private Iterator<WebPageEntity> get(String queryString) {
        Session session = database.getSessionFactory().openSession();

        Query query = session.createQuery(queryString);
        query.setCacheable(false);
        query.setReadOnly(true);

        ScrollableResults result = query.scroll(ScrollMode.FORWARD_ONLY);
        IterableListScrollableResults<WebPageEntity> webPageEntities = new IterableListScrollableResults<WebPageEntity>(session, result);

        return webPageEntities.iterator();
    }

    public Iterator<WebPageEntity> getUnparsedProductList() {
        String queryString = "from WebPageEntity where type = 'productList' and parsed = false order by rand()";
        return get(queryString);
    }

    public Iterator<WebPageEntity> getUnparsedProductPage() {
        String queryString = "from WebPageEntity where type = 'productPage' and parsed = false order by rand()";
        return get(queryString);
    }


    public Iterator<WebPageEntity> getUnparsedProductPageRaw() {
        String queryString = "from WebPageEntity where type = 'productPageRaw' and parsed = false order by rand()";
        return get(queryString);
    }
}
