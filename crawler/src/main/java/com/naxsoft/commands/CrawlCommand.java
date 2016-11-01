package com.naxsoft.commands;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.parsingService.WebPageService;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Crawl pages from initial data-set walking breath first. For each page generate one or more sub-pages to parse.
 * Stop at leafs.
 * Process the stream of unparsed webpages. Processed web pages are saved into the
 * database and the page is marked as parsed
 */
public class CrawlCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(CrawlCommand.class);

    private final WebPageService webPageService;
    private final WebPageParserFactory webPageParserFactory;
    private Disposable webPageParseSubscription;
    private Disposable parentMarkSubscription;

    @Inject
    public CrawlCommand(WebPageService webPageService, WebPageParserFactory webPageParserFactory) {
        this.webPageService = webPageService;
        this.webPageParserFactory = webPageParserFactory;
        webPageParseSubscription = null;
        parentMarkSubscription = null;
    }

    @Override
    public void setUp() throws CLIException {
    }


    @Override
    public void start() throws CLIException {
        Observable<WebPageEntity> webPageEntriesStream = Observable.interval(5, 5, TimeUnit.SECONDS).flatMap(i ->
                Observable.mergeDelayError(
                        webPageService.getUnparsedByType("frontPage"),
                        webPageService.getUnparsedByType("productList"),
                        webPageService.getUnparsedByType("productPage")))
                .retry()
                .publish()
                .autoConnect(2);

        webPageParseSubscription = webPageEntriesStream
                .doOnNext(webPageEntity -> LOGGER.trace("Starting parse {}", webPageEntity))
                .flatMap(webPageParserFactory::parse)
                .flatMap(webPageService::addWebPageEntry)
                .subscribe(
                        rc -> {
                            LOGGER.trace("Added WebPageEntry, parent marked as parsed: {} results added to DB", rc);
                        },
                        err -> LOGGER.error("Failed", err)
                );

        parentMarkSubscription = webPageEntriesStream
                .doOnNext(webPageEntity -> LOGGER.trace("Starting to mark as parsed {}", webPageEntity))
                .flatMap(webPageService::markParsed)
                .subscribe(
                        rc -> {
                            LOGGER.trace("Marked as parsed {}", rc);
                        },
                        err -> {
                            LOGGER.error("Maked as parsed failed", err);
                        });
    }

    @Override
    public void tearDown() throws CLIException {
        if (webPageParseSubscription != null) {
            webPageParseSubscription.dispose();
        }
        if (parentMarkSubscription != null) {
            parentMarkSubscription.dispose();
        }
    }
}
