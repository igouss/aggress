package com.naxsoft.commands;


import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsingService.WebPageService;
import com.naxsoft.utils.SitesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import javax.inject.Inject;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Add initial dataset to the database.
 * The crawling is basically breath first search from that dataset
 */
public class PopulateDBCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(PopulateDBCommand.class);

    private final WebPageService webPageService;

    @Inject
    public PopulateDBCommand(WebPageService webPageService) {
        this.webPageService = webPageService;
    }

    @Override
    public void setUp() throws io.vertx.core.cli.CLIException {
    }

    @Override
    public void start() throws CLIException {
        Observable<String> roots = Observable.from(SitesUtil.SOURCES);

        roots.observeOn(Schedulers.immediate())
                .subscribeOn(Schedulers.immediate())
                .map(entry -> new WebPageEntity(null, "", "frontPage", entry, ""))
                .flatMap(webPageService::addWebPageEntry)
                .all(result -> result != 0L)
                .subscribe(
                        result -> LOGGER.info("Roots populated: {}", result)
                        , err -> LOGGER.error("Failed to populate roots", err)
//                      , () -> LOGGER.info("Root population complete")
                );
    }

    @Override
    public void tearDown() throws CLIException {
    }
}
