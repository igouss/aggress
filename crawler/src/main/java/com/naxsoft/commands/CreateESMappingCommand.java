package com.naxsoft.commands;

import com.naxsoft.ExecutionContext;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.database.Elastic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 *
 * Create Elasticsearch mapping
 *
 */
public class CreateESMappingCommand implements Command{
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateESMappingCommand.class);

    private Elastic elastic = null;
    private HttpClient httpClient = null;
    private String indexSuffix = null;

    @Override
    public void setUp(ExecutionContext context) throws CLIException {
        elastic = context.getElastic();
        httpClient = context.getHTTPClient();
        indexSuffix = context.getIndexSuffix();
    }

    @Override
    public void run() throws CLIException {
        elastic.createMapping(httpClient, "product", "guns", indexSuffix)
                .subscribe(rc -> {
                    LOGGER.info("Elastic create mapping rc = {}", rc);
                }, ex -> {
                    LOGGER.error("CreateMapping Exception", ex);
                });
    }

    @Override
    public void tearDown() throws CLIException {
        elastic = null;
        httpClient = null;
        indexSuffix = null;
    }
}
