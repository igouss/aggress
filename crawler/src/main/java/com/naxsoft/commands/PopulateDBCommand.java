package com.naxsoft.commands;


import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsingService.WebPageService;
import com.naxsoft.utils.SitesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Add initial dataset to the database.
 * The crawling is basically breath first search from that dataset
 */
@Component
public class PopulateDBCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(PopulateDBCommand.class);

    private final WebPageService webPageService;

    @Autowired
    public PopulateDBCommand(WebPageService webPageService) {
        this.webPageService = webPageService;
    }

    @Override
    public void setUp() throws io.vertx.core.cli.CLIException {
    }

    @Override
    public void start() throws CLIException {
        Flux<String> roots = Flux.fromArray(SitesUtil.SOURCES);

        roots.map(entry -> WebPageEntity.legacyCreate(null, "", "frontPage", entry, ""))
                .flatMap(webPageService::addWebPageEntry)
                .all(result -> result != 0L)
                .subscribe(
                        result -> LOGGER.trace("Roots populated: {}", result)
                        , err -> LOGGER.error("Failed to populate roots", err)
                        , () -> LOGGER.info("Root population complete")
                );
    }

    @Override
    public void tearDown() throws CLIException {
    }
}
