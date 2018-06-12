package com.naxsoft.commands;

import com.naxsoft.storage.elasticsearch.Elastic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

/**
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
        elastic.createIndex("product", "guns");
    }

    @Override
    public void tearDown() throws CLIException {
    }
}
