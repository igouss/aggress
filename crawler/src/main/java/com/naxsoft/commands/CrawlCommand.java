package com.naxsoft.commands;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.parsingService.WebPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;

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
    private Subscription webPageParseSubscription;
    private Subscription parentMarkSubscription;

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
        Observable<WebPageEntity> webPageEntriesStream = Observable.merge(
                webPageService.getUnparsedByType("frontPage", 5, TimeUnit.SECONDS),
                webPageService.getUnparsedByType("productList", 5, TimeUnit.SECONDS),
                webPageService.getUnparsedByType("productPage", 5, TimeUnit.SECONDS));
        webPageEntriesStream
                .publish()
                .autoConnect(2);

        webPageParseSubscription = webPageEntriesStream
                .doOnNext(webPageEntity -> LOGGER.info("Starting parse {}", webPageEntity))
                .flatMap(webPageParserFactory::parse)
                .flatMap(webPageService::addWebPageEntry)
                .subscribe(
                        rc -> {
//                            LOGGER.info("Added WebPageEntry, parent marked as parsed: {} results added to DB", rc);
                        },
                        err -> LOGGER.error("Failed", err),
                        () -> LOGGER.info("Crawl completed")
                );

        parentMarkSubscription = webPageEntriesStream
                .doOnNext(webPageEntity -> LOGGER.info("Starting to mark as parsed {}", webPageEntity))
                .flatMap(webPageService::markParsed)
                .subscribe(
                        rc -> {
                            LOGGER.info("Maked as parsed {}", rc);
                        },
                        err -> {
                            LOGGER.info("Maked as parsed failed", err);
                        },
                        () -> {
                            LOGGER.info("Maked as parsed completed");
                        });
    }

    @Override
    public void tearDown() throws CLIException {
        if (webPageParseSubscription != null) {
            webPageParseSubscription.unsubscribe();
        }
        if (parentMarkSubscription != null) {
            parentMarkSubscription.unsubscribe();
        }
    }
}
