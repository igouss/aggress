package com.naxsoft.commands;

import com.naxsoft.ApplicationComponent;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.database.Elastic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.Semaphore;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Create Elasticsearch mapping
 */
public class CreateESMappingCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateESMappingCommand.class);

    @Inject
    protected Elastic elastic = null;
    @Inject
    protected HttpClient httpClient = null;
    @Inject
    protected String indexSuffix = null;

    @Override
    public void setUp(ApplicationComponent applicationComponent) throws CLIException {
        elastic = applicationComponent.getElastic();
        httpClient = applicationComponent.getHttpClient();
        indexSuffix = "";
    }

    @Override
    public void start() throws CLIException {
        Semaphore semaphore = new Semaphore(0);
        elastic.createMapping(httpClient, "product", "guns", indexSuffix)
                .subscribe(rc -> {
                            LOGGER.info("Elastic create mapping rc = {}", rc);
                        }, ex -> {
                            LOGGER.error("CreateMapping Exception", ex);
                            semaphore.release();
                        },
                        () -> {
                            LOGGER.info("Create mapping complete");
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
        elastic = null;
        httpClient = null;
        indexSuffix = null;
    }
}
