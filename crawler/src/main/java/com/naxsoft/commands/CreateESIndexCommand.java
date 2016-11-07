package com.naxsoft.commands;

import com.naxsoft.storage.elasticsearch.Elastic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Create Elasticsearch index
 */
public class CreateESIndexCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateESIndexCommand.class);

    private Elastic elastic = null;

    @Inject
    public CreateESIndexCommand(Elastic elastic) {
        this.elastic = elastic;
    }

    @Override
    public void setUp() throws CLIException {
    }

    @Override
    public void start() throws CLIException {
        LOGGER.trace("Elastic create index rc = {}", elastic.createIndex("product", "guns").blockingFirst());
    }

    @Override
    public void tearDown() throws CLIException {
    }
}
