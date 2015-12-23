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
        org.hibernate.Transaction tx = null;
        R result = null;
        try (Session session = database.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            result = action.call(session);
            tx.commit();
        } catch (Exception e) {
            if (null != tx) {
                tx.rollback();
            }
            logger.error("Transaction failed", e);
        }
        return result;
    }
}
