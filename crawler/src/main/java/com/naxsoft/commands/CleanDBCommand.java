package com.naxsoft.commands;


import com.naxsoft.ApplicationComponent;
import com.naxsoft.database.Persistent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.Semaphore;

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
            "WebPageEntity",
            "ProductEntity"
    };

    @Inject
    protected Persistent db = null;

    @Override
    public void setUp(ApplicationComponent applicationComponent) throws CLIException {
        db = applicationComponent.getDatabase();
    }

    @Override
    public void run() throws CLIException {
        Semaphore semaphore = new Semaphore(0);
        db.cleanUp(TABLES).subscribe(result -> {
                    LOGGER.info("Rows deleted: {}", result);
                },
                ex -> {
                    LOGGER.error("Crawler Process Exception", ex);
                },
                semaphore::release);
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tearDown() throws CLIException {
        db = null;
    }
}
