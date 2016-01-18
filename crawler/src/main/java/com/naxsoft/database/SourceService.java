//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.entity.SourceEntity;
import org.hibernate.Query;
import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Date;

/**
 *
 */
public class SourceService {
    private final static Logger LOGGER = LoggerFactory.getLogger(SourceService.class);
    private final Database database;

    /**
     *
     * @param database
     */
    public SourceService(Database database) {
        this.database = database;
    }

    /**
     *
     * @return
     */
    public Observable<SourceEntity> getSources() {
        String queryString = "from SourceEntity as s where s.enabled = true order by rand()";
        return Observable.defer(() -> new ObservableQuery<SourceEntity>(database).execute(queryString));
    }

    /**
     *
     * @param sourceEntity
     */
    public void markParsed(Observable<SourceEntity> sourceEntity) {
        sourceEntity.toList()
                .retry(3)
                .subscribe(list -> {
                    StatelessSession session = null;
                    org.hibernate.Transaction tx = null;
                    try {
                        session = database.getSessionFactory().openStatelessSession();
                        tx = session.beginTransaction();
                        Query query = session.createQuery("update WebPageEntity set modificationDate = :modificationDate where id = :id");


                        for (SourceEntity entry : list) {
                            query.setInteger("id", entry.getId());
                            query.setTimestamp("modificationDate", new Date());
                            query.executeUpdate();
                        }
                        tx.commit();
                    } catch (Exception e) {
                        LOGGER.error("Failed to mark as source as parsed", e);
                        if (null != tx) {
                            tx.rollback();
                        }
                    } finally {
                        if (null != session) {
                            session.close();
                        }
                    }
                }, ex -> LOGGER.error("MarkParsed Exception", ex));
    }

    /**
     *
     * @param sourceEntity
     * @return
     */
    public boolean save(SourceEntity sourceEntity) {
        return Transaction.execute(database, session -> {
            LOGGER.debug("Saving {}", sourceEntity);
            session.insert(sourceEntity);
            return true;
        });
    }
}
