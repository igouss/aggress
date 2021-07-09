package com.naxsoft.commands;


import com.naxsoft.storage.Persistent;
import lombok.extern.slf4j.Slf4j;


/**
 * Delete all the rows from previous crawling
 */
@Slf4j
public class CleanDBCommand implements Command {
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
