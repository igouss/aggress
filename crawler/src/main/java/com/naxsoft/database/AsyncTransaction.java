package com.naxsoft.database;

/**
 * Copyright NAXSoft 2015
 */

import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Func1;

public class AsyncTransaction {
    private static final Logger logger = LoggerFactory.getLogger(AsyncTransaction.class);

    public static <R> R execute(Database database, Func1<StatelessSession, R> action) {
        StatelessSession session = null;
        Transaction tx = null;
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
            logger.error("AsyncTransaction failed", e);
            throw e;
        } finally {
            if (null != session) {
                session.close();
            }
        }
        return result;
    }
}
