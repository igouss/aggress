package com.naxsoft.database;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
public class Database {
    private final Logger logger;
    private SessionFactory sessionFactory;

    public Database() {
        logger = LoggerFactory.getLogger(Database.class);
    }

    public void setUp() throws Exception {
        try {
            sessionFactory = new Configuration().configure().buildSessionFactory(
                    new StandardServiceRegistryBuilder().build());
        } catch (Exception e) {
            logger.error("Failed to create hibernate session factory", e);
            throw e;
        }
    }

    public void tearDown() {
        if (null != sessionFactory) {
            sessionFactory.close();
        }
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }


    public void save(Object entity) throws Exception {

        try {
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            session.save(entity);
            session.getTransaction().commit();
            session.close();
        } catch (HibernateException e) {
            logger.error("Failed to persist an entity" + entity, e);
            throw new Exception(e);
        }
    }
}
