//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.entity.SourceEntity;
import com.naxsoft.entity.WebPageEntity;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Date;

public class SourceService {
    private final static Logger logger = LoggerFactory.getLogger(SourceService.class);
    private final Database database;

    public SourceService(Database database) {
        this.database = database;
    }

    public Observable<SourceEntity> getSources() {
        String queryString = "from SourceEntity as s where s.enabled = true order by rand()";
        return Observable.defer(() -> new ObservableQuery<SourceEntity>(database).execute(queryString));
    }

    public void markParsed(Observable<SourceEntity> sourceEntity) {
        sourceEntity.toList().subscribe(list -> {
            Session session = database.getSessionFactory().openSession();
            Query query = session.createQuery("update WebPageEntity set modificationDate = :modificationDate where id = :id");
            Transaction tx = null;
            try {
                tx = session.beginTransaction();
                int count = 0;

                for (SourceEntity entry : list) {
                    query.setInteger("id", entry.getId());
                    query.setTimestamp("modificationDate", new Date());
                    query.executeUpdate();
                    ++count;
                    if (0 == count % 20) {
                        session.flush();
                    }
                }
                session.flush();
                tx.commit();
            } catch (Exception e) {
                logger.error("Failed to mark as source as parsed", e);
                if (null != tx) {
                    tx.rollback();
                }
            } finally {
                session.close();
            }
        });
    }
}
