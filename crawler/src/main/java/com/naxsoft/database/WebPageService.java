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

/**
 *
 */
public class WebPageService {
    private final static Logger LOGGER = LoggerFactory.getLogger(WebPageService.class);
    private final Database database;

    /**
     * @param database
     */
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
     * @param webPageEntity Page to update
     * @return The number of entities updated.
     */
    public int markParsed(WebPageEntity webPageEntity) {
        int rc = -1;
        if (0 == webPageEntity.getId()) {
            LOGGER.error("Trying to save a webpage with id=0 {}", webPageEntity);
        } else {
            rc = database.executeTransaction(session -> {
                Query query = session.createQuery("update WebPageEntity set parsed = true where id = :id");
                return query.setLong("id", webPageEntity.getId()).executeUpdate();
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
            try {
                long rc = 0L;
                do {
                    rc = database.executeQuery(session -> {
                        Long count = 0L;
                        String queryString = "select count (id) from WebPageEntity as w where w.parsed = false and w.type = :type";
                        Query query = session.createQuery(queryString);
                        query.setString("type", type);
                        count = (Long) query.list().get(0);
                        return count;
                    });
                    if (0 == rc) {
                        subscriber.onCompleted();
                    } else {
                        subscriber.onNext(rc);
                    }
                } while ((0L != rc) && !subscriber.isUnsubscribed());
            } catch (Exception e) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(e);
                }
            }
        });
    }

    /**
     * Get stream of unparsed pages.
     * Use scrolling
     * @param type Webpage type
     * @return Stream of unparsed pages of specefied type
     */
    public Observable<WebPageEntity> getUnparsedByType(String type) {
        final String query = "from WebPageEntity where type = '" + type + "' and parsed = false order by rand()";
        return getUnparsedCount(type).flatMap(f -> database.scroll(query));
    }
}
