//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.database.Database;
import com.naxsoft.database.IterableListScrollableResults;
import com.naxsoft.entity.SourceEntity;
import com.naxsoft.entity.WebPageEntity;
import java.util.Iterator;
import java.util.Set;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class WebPageService {
    private Database database;

    public WebPageService(Database database) {
        this.database = database;
    }

    public void save(SourceEntity source, Set<WebPageEntity> webPageEntitySet) {
        Session session = this.database.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        int i = 0;
        Iterator var6 = webPageEntitySet.iterator();

        while(var6.hasNext()) {
            WebPageEntity webPageEntity = (WebPageEntity)var6.next();
            webPageEntity.setSourceBySourceId(source);
            session.save(webPageEntity);
            ++i;
            if(i % 20 == 0) {
                session.flush();
                session.clear();
            }
        }

        session.flush();
        session.clear();
        tx.commit();
        session.close();
    }

    public void markParsed(Set<WebPageEntity> parsedProductList) {
        Session session = this.database.getSessionFactory().openSession();
        Query query = session.createQuery("update WebPageEntity set parsed = true where id = :id");
        Transaction tx = session.beginTransaction();
        int count = 0;
        Iterator var6 = parsedProductList.iterator();

        while(var6.hasNext()) {
            WebPageEntity webPageEntity = (WebPageEntity)var6.next();
            query.setInteger("id", webPageEntity.getId()).executeUpdate();
            ++count;
            if(count % 20 == 0) {
                session.flush();
                session.clear();
            }
        }

        session.flush();
        session.clear();
        tx.commit();
        session.close();
    }

    private Iterator<WebPageEntity> get(String queryString) {
        Session session = this.database.getSessionFactory().openSession();
        Query query = session.createQuery(queryString);
        query.setCacheable(false);
        query.setReadOnly(true);
        ScrollableResults result = query.scroll(ScrollMode.FORWARD_ONLY);
        IterableListScrollableResults webPageEntities = new IterableListScrollableResults(session, result);
        return webPageEntities.iterator();
    }

    public Iterator<WebPageEntity> getUnparsedProductList() {
        String queryString = "from WebPageEntity where type = \'productList\' and parsed = false order by rand()";
        return this.get(queryString);
    }

    public Iterator<WebPageEntity> getUnparsedProductPage() {
        String queryString = "from WebPageEntity where type = \'productPage\' and parsed = false order by rand()";
        return this.get(queryString);
    }

    public Iterator<WebPageEntity> getUnparsedProductPageRaw() {
        String queryString = "from WebPageEntity where type = \'productPageRaw\' and parsed = false order by rand()";
        return this.get(queryString);
    }
}
