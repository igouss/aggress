package com.naxsoft.commands;


import com.naxsoft.storage.Persistent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete all the rows from previous crawling
 */
public class CleanDBCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(CleanDBCommand.class);

    /**
     * Tables to purge
     */
    private final static String[] TABLES = {
            "WebPageEntity",
            "ProductEntity"
    };

    private final Persistent db;

    public CleanDBCommand(Persistent db) {
        this.db = db;
    }

    @Override
    public void setUp() throws CLIException {
    }

    @Override
    public void start() throws CLIException {
        db.cleanUp(TABLES);
    }

    @Override
    public void tearDown() throws CLIException {
    }
}
