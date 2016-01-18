//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Database implements AutoCloseable, Cloneable {
    private final static Logger LOGGER = LoggerFactory.getLogger(Database.class);
    private final SessionFactory sessionFactory;

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
     *
     */
    public void close() {
        if (null != this.sessionFactory) {
            this.sessionFactory.close();
        }

    }

    /**
     *
     * @return
     */
    public SessionFactory getSessionFactory() {
        return this.sessionFactory;
    }
}
