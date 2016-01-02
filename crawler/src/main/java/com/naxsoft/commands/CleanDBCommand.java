package com.naxsoft.commands;

import com.naxsoft.ExecutionContext;
import com.naxsoft.database.Database;
import org.hibernate.Query;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;

/**
 * Copyright NAXSoft 2015
 */
public class CleanDBCommand implements Command {
    private Database db;
    private final static String[] tables = {
            "SourceEntity",
            "WebPageEntity",
            "ProductEntity"
    };

    private void deleteOldData() {
        hqlTruncate(tables);
    }

    public void hqlTruncate(String[] tables) {
        StatelessSession statelessSession = db.getSessionFactory().openStatelessSession();
        Transaction tx = statelessSession.beginTransaction();
        for (String table : tables) {
            String hql = String.format("delete from %s", table);
            Query query = statelessSession.createQuery(hql);
            query.executeUpdate();
        }
        tx.commit();
        statelessSession.close();
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
