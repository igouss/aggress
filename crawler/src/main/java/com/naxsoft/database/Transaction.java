package com.naxsoft.database;

/**
 * Copyright NAXSoft 2015
 */

import org.hibernate.StatelessSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Func1;

/**
 *
 */
public class Transaction {
    private static final Logger LOGGER = LoggerFactory.getLogger(Transaction.class);

    /**
     *
     * @param database
     * @param action
     * @param <R>
     * @return
     */
    public static <R> R execute(Database database, Func1<StatelessSession, R> action) {
        StatelessSession session = null;
        org.hibernate.Transaction tx = null;
        R result = null;
        try {
            session = database.getSessionFactory().openStatelessSession();
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
        return result;
    }
}
