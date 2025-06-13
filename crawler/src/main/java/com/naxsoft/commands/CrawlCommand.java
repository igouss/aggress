package com.naxsoft.commands;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.parsingService.WebPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Crawl pages from initial data-set walking breath first. For each page generate one or more sub-pages to parse.
 * Stop at leafs.
 * Process the stream of unparsed webpages. Processed web pages are saved into the
 * database and the page is marked as parsed
 */
@Component
public class CrawlCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(CrawlCommand.class);

    private final WebPageService webPageService;
    private final WebPageParserFactory webPageParserFactory;
    private Disposable webPageParseSubscription;
    private Disposable parentMarkSubscription;

    @Autowired
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
        Flux<WebPageEntity> webPageEntriesStream = Flux.interval(Duration.ofSeconds(5), Duration.ofSeconds(5))
                .flatMap(i -> Flux.mergeDelayError(8,
                        webPageService.getUnparsedByType("frontPage"),
                        webPageService.getUnparsedByType("productList"),
                        webPageService.getUnparsedByType("productPage")))
                .retry()
                .share();

        webPageParseSubscription = webPageEntriesStream
                .doOnNext(webPageEntity -> LOGGER.trace("Starting parse {}", webPageEntity))
                .flatMap(webPageParserFactory::parse)
                .flatMap(webPageService::addWebPageEntry)
                .subscribe(
                        rc -> {
                            LOGGER.trace("Added WebPageEntry, parent marked as parsed: {} results added to DB", rc);
                        },
                        err -> LOGGER.error("Failed", err),
                        () -> LOGGER.info("Crawl completed")
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
                        },
                        () -> {
                            LOGGER.info("Marked as parsed completed");
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
