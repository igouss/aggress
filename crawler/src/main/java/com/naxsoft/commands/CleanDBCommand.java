package com.naxsoft.commands;


import com.naxsoft.ApplicationComponent;
import com.naxsoft.database.Database;
import com.naxsoft.providers.DatabaseModule;
import com.naxsoft.providers.DatabaseModule_GetFactory;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

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
    protected Database db = null;


    /**
     * Delete all data from the database tables
     *
     * @param tables Database tables to purge
     */
    private void hqlTruncate(String[] tables) {
        for (String table : tables) {
            int rc = db.executeTransaction(session -> {
                String hql = String.format("delete from %s", table);
                Query query = session.createQuery(hql);
                return query.executeUpdate();
            });
            LOGGER.debug("Deleted {} records from the table {}", rc, table);
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
