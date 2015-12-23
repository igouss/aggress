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
    private static final int BATCH_SIZE = 20;
    private final Database database;

    public ObservableQuery(Database database) {
        this.database = database;
    }

    public Observable<T> execute(String queryString) {
        return Observable.using(this::getStatelessSession,
                session -> executeQuery(queryString, session),
                StatelessSession::close);
    }

    private StatelessSession getStatelessSession() {
        return database.getSessionFactory().openStatelessSession();
    }

    private Observable<T> executeQuery(String queryString, StatelessSession session) {
        return Observable.using(() -> getScrollableResults(queryString, session),
                this::scrollResults,
                ScrollableResults::close);
    }

    private static ScrollableResults getScrollableResults(String queryString, StatelessSession session) {
        Query query = session.createQuery(queryString);
        query.setCacheable(false);
        query.setReadOnly(true);
        query.setFetchSize(BATCH_SIZE);
        return query.scroll(ScrollMode.FORWARD_ONLY);
    }

    private Observable<T> scrollResults(ScrollableResults result) {
        return Observable.<T>create(subscriber -> {
            while (!subscriber.isUnsubscribed() && result.next()) {
                subscriber.onNext((T) result.get(0));
            }
            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        });
    }
}
