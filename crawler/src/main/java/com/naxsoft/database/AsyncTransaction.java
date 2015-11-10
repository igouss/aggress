package com.naxsoft.database;

/**
 * Copyright NAXSoft 2015
 */

import org.hibernate.Session;
import rx.functions.Func1;

public class AsyncTransaction {
    public static <R> R execute(Database database, Func1<Session, R> action) {
        Session session = database.getSessionFactory().openSession();
        org.hibernate.Transaction tx = null;
        try {
            tx = session.beginTransaction();
            R result = action.call(session);
            session.flush();
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx != null) {
                session.flush();
                tx.commit();
            }
            throw e;
        } finally {
            session.close();
        }
    }
}
