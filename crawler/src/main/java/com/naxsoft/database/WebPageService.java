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

import static java.lang.System.out;

public class WebPageService {
    private final static Logger logger = LoggerFactory.getLogger(WebPageService.class);
    private final Database database;
    private final ObservableQuery<WebPageEntity> observableQuery;

    public WebPageService(Database database) {
        this.database = database;
        observableQuery = new ObservableQuery<>(database);
    }

    public boolean save(WebPageEntity webPageEntity) {
        Boolean rc = false;
        try {
            rc = Transaction.execute(database, session -> {
                logger.debug("Saving {}", webPageEntity);
                session.insert(webPageEntity);
                return true;
            });
        } catch (ConstraintViolationException ex) {
            logger.info("A duplicate URL found, ignore", ex);
        }
        return rc;
    }

    public int markParsed(WebPageEntity webPageEntity) {
        if (0 == webPageEntity.getId()) {
            return 0;
        }
        return Transaction.execute(database, session -> {
            Query query = session.createQuery("update WebPageEntity set parsed = true where id = :id");
            return query.setInteger("id", webPageEntity.getId()).executeUpdate();
        });
    }

    /**
     * Keep producing unparsedCount till it is equals to 0
     *
     * @param type Column type to check against
     * @return row count in type
     */
    public Observable<Long> getUnparsedCount(String type) {
        return Observable.create(subscriber -> {
            Long rc = 0L;
            try {
                do {
                    rc = Transaction.execute(database, session -> {
                        Long count = 0L;
                        String queryString = "select count (id) from WebPageEntity as w where w.parsed = false and w.type = :type";
                        Query query = session.createQuery(queryString);
                        query.setString("type", type);
                        count = (Long) query.list().get(0);
                        return count;
                    });
                    if (null == rc) {
                        subscriber.onError(new Exception("Could not get unparsed count for " + type));
                    } else if (0 != rc) {
                        subscriber.onNext(rc);
                    } else {
                        subscriber.onCompleted();
                    }
                } while ((0L != rc) && !subscriber.isUnsubscribed());
            } catch (Exception e) {
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public Observable<WebPageEntity> getUnparsedByType(String type) {
        final String query = "from WebPageEntity where type = '" + type + "' and parsed = false order by rand()";
        return getUnparsedCount(type)
                .doOnNext(value -> out.println("Found " + value + " unparsed webpages of type " + type))
                .doOnError(ex -> logger.error("Exception", ex))
                .flatMap(count -> observableQuery.execute(query));
    }
}
