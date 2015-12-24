package com.naxsoft.database;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
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
        return Observable.using(this::getSession,
                session -> executeQuery(queryString, session),
                Session::close);
    }

    private Session getSession() {
        return database.getSessionFactory().openSession();
    }

    private Observable<T> executeQuery(String queryString, Session session) {
        return Observable.using(() -> getScrollableResults(queryString, session),
                this::scrollResults,
                ScrollableResults::close);
    }

    private static ScrollableResults getScrollableResults(String queryString, Session session) {
        /* hack? */
        if (!session.isOpen()) {
            session = session.getSessionFactory().openSession();
        }
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
