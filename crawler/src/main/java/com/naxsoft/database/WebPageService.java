//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.entity.WebPageEntity;
import org.hibernate.Query;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 *
 */
@Singleton
public class WebPageService {
    private final static Logger LOGGER = LoggerFactory.getLogger(WebPageService.class);

    @Inject
    protected Database database;

    /**
     * @param database
     */
    @Inject
    public WebPageService(Database database) {
        this.database = database;
    }

    /**
     * @param webPageEntity
     * @return
     */
    public boolean save(WebPageEntity webPageEntity) {
        Boolean rc = false;
        try {
            rc = database.executeTransaction(session -> {
                LOGGER.debug("Saving {}", webPageEntity);
                session.insert(webPageEntity);
                return true;
            });
        } catch (ConstraintViolationException ex) {
            LOGGER.info("A duplicate URL found, ignore", ex);
        }
        return rc;
    }

    /**
     * Update page parsed status in the database
     *
     * @param webPageEntity Page to update
     * @return The number of entities updated.
     */
    public int markParsed(WebPageEntity webPageEntity) {
        int rc = -1;
        if (0 == webPageEntity.getId()) {
//            LOGGER.error("Trying to save a webpage with id=0 {}", webPageEntity);
            throw new RuntimeException("Trying to save a webpage with id=0" + webPageEntity);
        } else {
            rc = database.executeTransaction(session -> {
                Query query = session.createQuery("update WebPageEntity set parsed = true where id = :id");
                query.setLong("id", webPageEntity.getId());
                int affectedRows = query.executeUpdate();
                return affectedRows;
            });
        }
        return rc;
    }

    /**
     * Keep producing unparsedCount till it is equals to 0
     *
     * @param type Column type to check against
     * @return row count in type
     */
    public Observable<Long> getUnparsedCount(String type) {
        return Observable.create(subscriber -> {
            while (!subscriber.isUnsubscribed()) {
                long rowCount = database.executeQuery(session -> {
                    Long count = 0L;
                    String queryString = "select count (id) from WebPageEntity as w where w.parsed = false and w.type = :type";
                    Query query = session.createQuery(queryString);
                    query.setString("type", type);
                    count = (Long) query.list().get(0);
                    return count;
                });
                LOGGER.info("Unparsed number of entries of type {} is {}", type, rowCount);
                if (0L == rowCount) {
                    subscriber.onCompleted();
                    break;
                } else {
                    subscriber.onNext(rowCount);
                }
            }
        });
    }

    /**
     * Get stream of unparsed pages.
     * Use scrolling
     *
     * @param type Webpage type
     * @return Stream of unparsed pages of specefied type
     */
    public Observable<WebPageEntity> getUnparsedByType(String type) {
        final String query = "from WebPageEntity where type = '" + type + "' and parsed = false order by rand()";
        return getUnparsedCount(type).flatMap(count -> {
            LOGGER.info("Scrolling over {} sql {}", count, query);
            return database.scroll(query);
        });
    }
}
