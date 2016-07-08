package com.naxsoft.database;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func1;

import javax.inject.Singleton;

/**
 * Database abstraction.
 */
@Singleton
public class Database implements Persistent {
    private final static Logger LOGGER = LoggerFactory.getLogger(Database.class);
    private static final Long BATCH_SIZE = 20L;
    private SessionFactory sessionFactory;

    /**
     *
     */
    public Database() {
        try {
            StandardServiceRegistry e = (new StandardServiceRegistryBuilder()).configure().build();
            Metadata metadata = (new MetadataSources(e)).getMetadataBuilder().build();
            this.sessionFactory = metadata.getSessionFactoryBuilder().build();
        } catch (Exception e) {
            LOGGER.error("Failed to create hibernate session factory", e);
            throw e;
        }
    }

    /**
     * Avoid loading whole dataset, instead use scrollable result.
     *
     * @param queryString SQL query to execute
     * @param session     Database session connection
     * @return Scrollable result generated by the query
     */
    private static ScrollableResults getScrollableResults(String queryString, Long count, StatelessSession session) {
        Query query = session.createQuery(queryString);
        query.setCacheable(false);
        query.setReadOnly(true);
        query.setFetchSize(count.intValue());
        query.setMaxResults(count.intValue());
        LOGGER.info("Scroll SQL {}", query.getQueryString());
        return query.scroll(ScrollMode.FORWARD_ONLY);
    }

    /**
     * Get the observable result. Rows are loaded one at time from the database.
     *
     * @param result Record set over which to iterate
     * @return Observable result stream
     */
    private static <T> Observable<T> scrollResults(ScrollableResults result) {
        return Observable.create(subscriber -> {
            while (!subscriber.isUnsubscribed() && result.next()) {
                LOGGER.info("Scroll row# {}", result.getRowNumber());
                T t = (T) result.get(0);
                if (null == t) {
                    subscriber.onError(new Exception("Unexpected result"));
                } else {
                    subscriber.onNext(t);
                }
            }
            subscriber.onCompleted();
        });
    }


    @Override
    public void close() {
        if (null != sessionFactory && !sessionFactory.isClosed()) {
            sessionFactory.close();
            sessionFactory = null;
        }
    }

    @Override
    public Observable<Long> getUnparsedCount(String type) {
        Long result = executeQuery(session -> {
            Long count = 0L;
            String queryString = "select count (id) from WebPageEntity as w where w.parsed = false and w.type = :type";
            Query query = session.createQuery(queryString);
            query.setParameter("type", type);
            count = (Long) query.list().get(0);
            return count;
        });
        return Observable.just(result);
    }

    @Override
    public Observable<Integer> markWebPageAsParsed(WebPageEntity webPageEntity) {
        if (webPageEntity == null) {
            return Observable.error(new Exception("Trying to mark null WebPageEntity as parsed"));
        }
        return executeTransaction(session -> {
            Query query = session.createQuery("update WebPageEntity set parsed = true where id = :id");
            query.setParameter("id", webPageEntity.getId());
            return query.executeUpdate();
        });
    }

    @Override
    public Observable<Integer> markAllProductPagesAsIndexed() {
        return executeTransaction(session -> {
            Query query = session.createQuery("update ProductEntity as p set p.indexed = true");
            return query.executeUpdate();
        });
    }

    @Override
    public Observable<Long> save(ProductEntity productEntity) {
        Observable<Long> rc;
        try {
            rc = executeTransaction(session -> {
                LOGGER.debug("Saving {}", productEntity);
                session.insert(productEntity);
                return 1L;
            });
        } catch (ConstraintViolationException ex) {
            LOGGER.info("A duplicate URL found, ignore", ex);
            rc = Observable.just(0L);
        }
        return rc;
    }

    @Override
    public Observable<Long> save(WebPageEntity webPageEntity) {
        Observable<Long> rc;
        try {
            rc = executeTransaction(session -> {
                LOGGER.debug("Saving {}", webPageEntity);
                session.insert(webPageEntity);
                return 1L;
            });
        } catch (ConstraintViolationException ex) {
            LOGGER.info("A duplicate URL found, ignore", ex);
            rc = Observable.just(0L);
        }
        return rc;
    }

    @Override
    public Observable<ProductEntity> getProducts() {
        String queryString = "from ProductEntity";
        return scroll(queryString, BATCH_SIZE);
    }

    @Override
    public Observable<WebPageEntity> getUnparsedByType(String type, Long count) {
        final String query = "from WebPageEntity where type = '" + type + "' and parsed = false order by rand()";
        return scroll(query, count);
    }

    @Override
    public Observable<Long> cleanUp(String[] tables) {
        Observable<Long> result = Observable.empty();
        for (String table : tables) {
            result = Observable.concat(result, executeTransaction(session -> {
                String hql = String.format("delete from %s", table);
                Query query = session.createQuery(hql);
                return query.executeUpdate();
            }).map(Integer::longValue));
        }
        return result;
    }

    /**
     * @param action
     * @param <R>
     * @return
     */
    private <R> R executeQuery(Func1<StatelessSession, R> action) {
        StatelessSession session = null;
        R result = null;
        try {
            session = sessionFactory.openStatelessSession();
            result = action.call(session);
        } finally {
            if (null != session) {
                session.close();
            }
        }
        return result;
    }

    /**
     * @param <R>
     * @param action
     * @return
     */
    public <R> Observable<R> executeTransaction(Func1<StatelessSession, R> action) {
        StatelessSession session = null;
        org.hibernate.Transaction tx = null;
        R result = null;
        try {
            session = sessionFactory.openStatelessSession();
            tx = session.beginTransaction();
            result = action.call(session);
            tx.commit();
        } catch (Exception e) {
            if (null != tx) {
                tx.rollback();
            }
            LOGGER.error("Transaction failed", e);
            throw e;
        } finally {
            if (null != session) {
                session.close();
            }
        }
        return Observable.just(result);
    }

    /**
     * Avoid loading whole result set generated by the query. Instead iterate over scrollable result set
     *
     * @param queryString SQL query to execute
     * @return Stream of elements returned by SQL query.
     */
    private <T> Observable<T> scroll(String queryString, Long count) {
        return Observable.using(sessionFactory::openStatelessSession,
                session -> doScroll(queryString, count, session),
                StatelessSession::close);
    }

    /**
     * Avoid loading whole result set generated by the query. Instead iterate over scrollable result set
     *
     * @param queryString SQL query to execute
     * @param session     Database session
     * @return Stream of elements returned by SQL query.
     */
    private <T> Observable<T> doScroll(String queryString, Long count, StatelessSession session) {
        return Observable.using(() -> getScrollableResults(queryString, count, session),
                Database::scrollResults,
                ScrollableResults::close);
    }
}
