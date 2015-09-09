//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.entity.WebPageEntity;
import org.hibernate.*;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class WebPageService {
    private Database database;

    public WebPageService(Database database) {
        this.database = database;
    }

    public void save(Set<WebPageEntity> webPageEntitySet) {
        Session session = this.database.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        int i = 0;
        Iterator var6 = webPageEntitySet.iterator();

        while(var6.hasNext()) {
            WebPageEntity webPageEntity = (WebPageEntity)var6.next();
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

    public void markParsed(Collection<WebPageEntity> parsedProductList) {
        Session session = this.database.getSessionFactory().openSession();
        Query query = session.createQuery("update WebPageEntity set parsed = true where id = :id");
        Transaction tx = session.beginTransaction();
        int count = 0;

        for(WebPageEntity webPageEntity : parsedProductList) {
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

    private List<WebPageEntity> get(String queryString) {
        Session session = this.database.getSessionFactory().openSession();
        Query query = session.createQuery(queryString);
        query.setCacheable(false);
        query.setReadOnly(true);
        ScrollableResults result = query.scroll(ScrollMode.FORWARD_ONLY);
        IterableListScrollableResults webPageEntities = new IterableListScrollableResults(session, result);
        return webPageEntities;
    }

    public List<WebPageEntity> getUnparsedProductList() {
        String queryString = "from WebPageEntity where type = \'productList\' and parsed = false order by rand()";
        return this.get(queryString);
    }

    public List<WebPageEntity> getUnparsedProductPage() {
        String queryString = "from WebPageEntity where type = \'productPage\' and parsed = false order by rand()";
        return this.get(queryString);
    }

    public List<WebPageEntity> getUnparsedProductPageRaw() {
        String queryString = "from WebPageEntity where type = \'productPageRaw\' and parsed = false order by rand()";
        return this.get(queryString);
    }

    public List<WebPageEntity> getUnparsedFrontPage() {
        String queryString = "from WebPageEntity where type = \'frontPage\' and parsed = false order by rand()";
        return this.get(queryString);
    }

    public List<WebPageEntity> getUnparsedPage() {
        String queryString = "from WebPageEntity where parsed = false order by rand()";
        return this.get(queryString);
    }
}
