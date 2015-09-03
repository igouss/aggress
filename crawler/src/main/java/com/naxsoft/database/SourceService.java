package com.naxsoft.database;

import com.naxsoft.entity.SourceEntity;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.Iterator;

/**
 * Copyright NAXSoft 2015
 */
public class SourceService {

    private SessionFactory sessionFactory;

    public SourceService(Database database) {
        sessionFactory = database.getSessionFactory();
    }

    public Iterator<SourceEntity> getSources() {

            Session session = sessionFactory.openSession();
            ScrollableResults result = session.createQuery("from SourceEntity order by rand()").scroll();
            return new IterableListScrollableResults<SourceEntity>(session, result).iterator();

    }
}
