//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {
    private final Logger logger = LoggerFactory.getLogger(Database.class);
    private SessionFactory sessionFactory;

    public Database() {
    }

    public void setUp() throws Exception {
        try {
            StandardServiceRegistry e = (new StandardServiceRegistryBuilder()).configure().build();
            Metadata metadata = (new MetadataSources(e)).getMetadataBuilder().build();
            this.sessionFactory = metadata.getSessionFactoryBuilder().build();
        } catch (Exception var3) {
            this.logger.error("Failed to create hibernate session factory", var3);
            throw var3;
        }
    }

    public void tearDown() {
        if(null != this.sessionFactory) {
            this.sessionFactory.close();
        }

    }

    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }

    public void save(Object entity) throws Exception {
        try {
            Session e = this.sessionFactory.openSession();
            e.beginTransaction();
            e.save(entity);
            e.getTransaction().commit();
            e.close();
        } catch (HibernateException var3) {
            this.logger.error("Failed to persist an entity" + entity, var3);
            throw new Exception(var3);
        }
    }
}
