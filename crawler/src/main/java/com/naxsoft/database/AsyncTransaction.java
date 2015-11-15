package com.naxsoft.database;

/**
 * Copyright NAXSoft 2015
 */

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Func1;

public class AsyncTransaction {
    private static final Logger logger = LoggerFactory.getLogger(AsyncTransaction.class);

    public static <R> R execute(Database database, Func1<Session, R> action) {
        Session session = database.getSessionFactory().openSession();
        org.hibernate.Transaction tx = null;
        R result = null;
        try {
            tx = session.beginTransaction();
            result = action.call(session);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error("Transaction failed", e);
        } finally {
            session.close();
        }
        return result;
    }
}
