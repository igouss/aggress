package com.naxsoft.commands;


import com.naxsoft.ApplicationComponent;
import com.naxsoft.database.Persistent;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Delete all the rows from previous crawling
 */
public class CleanDBCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(CleanDBCommand.class);

    /**
     * Tables to purge
     */
    private final static String[] TABLES = {
            "SourceEntity",
            "WebPageEntity",
            "ProductEntity"
    };

    @Inject
    protected Persistent db = null;


    /**
     * Delete all data from the database tables
     *
     * @param tables Database tables to purge
     */
    private void hqlTruncate(String[] tables) {
        for (String table : tables) {
            db.executeTransaction(session -> {
                String hql = String.format("delete from %s", table);
                Query query = session.createQuery(hql);
                return query.executeUpdate();
            }).subscribe(value -> {
                LOGGER.debug("Deleted {} records from the table {}", value, table);
            });
        }
    }

    @Override
    public void setUp(ApplicationComponent applicationComponent) throws CLIException {
        db = applicationComponent.getDatabase();
    }

    @Override
    public void run() throws CLIException {
        hqlTruncate(TABLES);
    }

    @Override
    public void tearDown() throws CLIException {
        db = null;
    }
}
