package com.naxsoft.database;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;
import rx.Observable;

/**
 * Copyright NAXSoft 2015
 */
public class ObservableQuery<T> {
    private Database database;

    public ObservableQuery(Database database) {
        this.database = database;
    }

    private Observable<T> executeQuery(String queryString, StatelessSession session) {
        return Observable.using(() -> getScrollableResults(queryString, session),
                scrollableResults -> scrollResults(scrollableResults),
                scrollableResults -> scrollableResults.close());
    }

    private ScrollableResults getScrollableResults(String queryString, StatelessSession session) {
        Query query = session.createQuery(queryString);
        query.setCacheable(false);
        query.setReadOnly(true);
        return query.scroll(ScrollMode.FORWARD_ONLY);
    }

    private Observable<T> scrollResults(ScrollableResults result) {
        return Observable.<T>create(o -> {
            while (result.next()) {
                o.onNext((T) result.get(0));
            }
            o.onCompleted();
        });
    }

    private StatelessSession getStatelessSession() {
        return database.getSessionFactory().openStatelessSession();
    }

    public Observable<T> execute(String queryString) {
        return Observable.defer(() -> Observable.using(() -> getStatelessSession(),
                session -> executeQuery(queryString, session),
                session -> session.close()));
    }

}
