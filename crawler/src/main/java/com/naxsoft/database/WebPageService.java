//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.entity.WebPageEntity;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class WebPageService {
    private final static Logger logger = LoggerFactory.getLogger(WebPageService.class);
    private final Database database;
    private final ObservableQuery<WebPageEntity> observableQuery;

    public WebPageService(Database database) {
        this.database = database;
        observableQuery = new ObservableQuery<>(database);
    }

    public boolean save(Collection<WebPageEntity> webPageEntitySet) {
        return AsyncTransaction.execute(database, session -> {
            int i = 0;
            for (WebPageEntity webPageEntity : webPageEntitySet) {
                logger.debug("Saving {}", webPageEntity);
                session.save(webPageEntity);
                if (0 == ++i % 20) {
                    session.flush();
                }
            }
            return true;
        });
    }

    public int markParsed(WebPageEntity webPageEntity) {
        return AsyncTransaction.execute(database, session -> {
            Query query = session.createQuery("update WebPageEntity set parsed = true where id = :id");
            int rc = query.setInteger("id", webPageEntity.getId()).executeUpdate();
            logger.debug("update WebPageEntity set parsed = true where id = {} {} rows affected", webPageEntity.getId(), rc);
            return rc;
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
                    rc = AsyncTransaction.execute(database, session -> {
                        Long count = 0L;
                        String queryString = "select count (id) from WebPageEntity as w where w.parsed = false and w.type = :type";
                        Query query = session.createQuery(queryString);
                        query.setString("type", type);
                        count = (Long) query.list().get(0);
                        return count;
                    });
                    if (0 != rc) {
                        subscriber.onNext(rc);
                    } else {
                        subscriber.onCompleted();
                    }
                } while (0 != rc || !subscriber.isUnsubscribed());
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    public Observable<WebPageEntity> getUnparsedProductList() {
        final String queryString = "from WebPageEntity where type = 'productList' and parsed = false order by rand()";
        return executeQuery(queryString, "productList");
    }

    public Observable<WebPageEntity> getUnparsedProductPage() {
        final String queryString = "from WebPageEntity where type = 'productPage' and parsed = false order by rand()";
        return executeQuery(queryString, "productPage");
    }

    public Observable<WebPageEntity> getUnparsedProductPageRaw() {
        final String queryString = "from WebPageEntity where type = 'productPageRaw' and parsed = false order by rand()";
        return executeQuery(queryString, "productPageRaw");
    }

    public Observable<WebPageEntity> getUnparsedFrontPage() {
        final String queryString = "from WebPageEntity where type = 'frontPage' and parsed = false order by rand()";
        return executeQuery(queryString, "frontPage");
    }

    private Observable<WebPageEntity> executeQuery(String query, String condColumn) {
        return getUnparsedCount(condColumn).flatMap(count -> observableQuery.execute(query));
    }
}
