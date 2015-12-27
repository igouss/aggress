package com.naxsoft.database;

import org.hibernate.*;
import rx.Observable;

import java.sql.SQLException;

/**
 * Copyright NAXSoft 2015
 */
public class ObservableQuery<T> {
    private static final int BATCH_SIZE = 20;
    private final Database database;

    public ObservableQuery(Database database) {
        this.database = database;
    }

    private static ScrollableResults getScrollableResults(String queryString, StatelessSession session) {
        /* hack? */
//        if (!session.isOpen()) {
//            session = session.getSessionFactory().openSession();
//        }
        Query query = session.createQuery(queryString);
        query.setCacheable(false);
        query.setReadOnly(true);
        query.setFetchSize(BATCH_SIZE);
        return query.scroll(ScrollMode.FORWARD_ONLY);
    }

    public Observable<T> execute(String queryString) {
        return Observable.using(this::getSession,
                session -> executeQuery(queryString, session),
                StatelessSession::close);
    }

    private StatelessSession getSession() {
        return database.getSessionFactory().openStatelessSession();
    }

    private Observable<T> executeQuery(String queryString, StatelessSession session) {
        return Observable.using(() -> getScrollableResults(queryString, session),
                this::scrollResults,
                ScrollableResults::close);
    }

    private Observable<T> scrollResults(ScrollableResults result) {
        return Observable.<T>create(subscriber -> {
            while (!subscriber.isUnsubscribed() && result.next()) {
                T t = (T) result.get(0);
                if (null == t) {
                    subscriber.onError(new Exception("Unexpected result"));
                } else {
                    subscriber.onNext(t);
                }
            }
            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        });
    }
}
