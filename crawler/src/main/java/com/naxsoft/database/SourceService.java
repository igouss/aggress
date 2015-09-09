//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.entity.SourceEntity;
import org.hibernate.*;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SourceService {
    private SessionFactory sessionFactory;

    public SourceService(Database database) {
        this.sessionFactory = database.getSessionFactory();
    }

    public List<SourceEntity> getSources() {
        Session session = this.sessionFactory.openSession();
        ScrollableResults result = session.createQuery("from SourceEntity as s where s.enabled = true order by rand()").scroll();
        return new IterableListScrollableResults(session, result);
    }

    public void markParsed(Set<SourceEntity> sourceEntities) {
        Session session = this.sessionFactory.openSession();
        Query query = session.createQuery("update WebPageEntity set modificationDate = :modificationDate where id = :id");
        Transaction tx = session.beginTransaction();
        int count = 0;
        Iterator var6 = sourceEntities.iterator();

        while(var6.hasNext()) {
            SourceEntity sourceEntity = (SourceEntity)var6.next();
            query.setInteger("id", sourceEntity.getId());
            query.setTimestamp("modificationDate", new Date());
            query.executeUpdate();
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
}
