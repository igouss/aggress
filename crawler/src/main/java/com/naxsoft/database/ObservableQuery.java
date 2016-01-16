package com.naxsoft.database;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.StatelessSession;
import rx.Observable;

/**
 * Copyright NAXSoft 2015
 *
 *
 */
public class ObservableQuery<T> {
    private static final int BATCH_SIZE = 20;
    private final Database database;

    /**
     *
     * @param database
     */
    public ObservableQuery(Database database) {
        this.database = database;
    }

    /**
     *
     * @param queryString
     * @param session
     * @return
     */
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

    /**
     *
     * @param queryString
     * @return
     */
    public Observable<T> execute(String queryString) {
        return Observable.using(this::getSession,
                session -> executeQuery(queryString, session),
                StatelessSession::close);
    }

    /**
     *
     * @return
     */
    private StatelessSession getSession() {
        return database.getSessionFactory().openStatelessSession();
    }

    /**
     *
     * @param queryString
     * @param session
     * @return
     */
    private Observable<T> executeQuery(String queryString, StatelessSession session) {
        return Observable.using(() -> getScrollableResults(queryString, session),
                this::scrollResults,
                ScrollableResults::close);
    }

    /**
     *
     * @param result
     * @return
     */
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
