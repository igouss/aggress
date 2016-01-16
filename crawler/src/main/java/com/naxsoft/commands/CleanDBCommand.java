package com.naxsoft.commands;

import com.naxsoft.ExecutionContext;
import com.naxsoft.database.Database;
import org.hibernate.Query;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 *
 * Delete all the rows from previous crawling
 *
 */
public class CleanDBCommand implements Command {
    private final static Logger logger = LoggerFactory.getLogger(CleanDBCommand.class);

    /**
     *
     */
    private final static String[] tables = {
            "SourceEntity",
            "WebPageEntity",
            "ProductEntity"
    };
    private Database db = null;

    private void deleteOldData() {
        hqlTruncate(tables);
    }

    /**
     *
     * @param tables
     */
    public void hqlTruncate(String[] tables) {
        StatelessSession statelessSession = db.getSessionFactory().openStatelessSession();
        Transaction tx = null;
        try {
            tx = statelessSession.beginTransaction();
            for (String table : tables) {
                String hql = String.format("delete from %s", table);
                Query query = statelessSession.createQuery(hql);
                query.executeUpdate();
            }
            tx.commit();
        } catch (Exception e) {
            logger.error("Failed to clean-up database", e);
            if (null != tx) {
                tx.rollback();
            }
        } finally {
            statelessSession.close();
        }
    }

    @Override
    public void setUp(ExecutionContext context) throws CLIException {
        db = context.getDb();
    }

    @Override
    public void run() throws CLIException {
        deleteOldData();
    }

    @Override
    public void tearDown() throws CLIException {
        db = null;
    }
}
