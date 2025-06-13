package com.naxsoft.commands;

import com.naxsoft.storage.elasticsearch.Elastic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.Semaphore;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Create Elasticsearch index
 */
@Component
public class CreateESIndexCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateESIndexCommand.class);

    private Elastic elastic = null;

    @Autowired
    public CreateESIndexCommand(Elastic elastic) {
        this.elastic = elastic;
    }

    @Override
    public void setUp() throws CLIException {
    }

    @Override
    public void start() throws CLIException {
        Semaphore semaphore = new Semaphore(0);
        elastic.createIndex("product", "guns")
                .subscribe(
                        rc -> {
                            LOGGER.trace("Elastic create index rc = {}", rc);
                        },
                        ex -> {
                            LOGGER.error("CreateIndex Exception", ex);
                            semaphore.release();
                        },
                        () -> {
                            LOGGER.info("CreateIndex complete");
                            semaphore.release();
                        });
        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void tearDown() throws CLIException {
    }
}
