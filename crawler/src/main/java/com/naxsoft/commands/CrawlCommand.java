package com.naxsoft.commands;

import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.parsingService.WebPageService;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
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
    private Disposable connection;

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
        connection = Flowable.interval(5, TimeUnit.SECONDS)
                .onBackpressureDrop()
                .flatMap(i -> Flowable.concat(
                        webPageService.getUnparsedByType("frontPage"),
                        webPageService.getUnparsedByType("productList"),
                        webPageService.getUnparsedByType("productPage")))
                .onBackpressureBuffer()
                .doOnCancel(() -> LOGGER.warn("parse cancel called"))
                .doOnError(throwable -> LOGGER.error("Error", throwable))
                .doOnNext(webPageEntity -> LOGGER.info("Starting parse {}", webPageEntity))
                .subscribeOn(Schedulers.io())
                .buffer(1, TimeUnit.SECONDS)
                .onBackpressureBuffer()
                .doOnNext(webPageService::markParsed)
                .subscribeOn(Schedulers.io())
                .flatMap(webPageParserFactory::parse)
                .buffer(1, TimeUnit.SECONDS)
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .map(webPageService::addWebPageEntry)
                .subscribe(val -> {
                    LOGGER.info("Added new page {}", val);
                }, err -> {
                    LOGGER.error("Crawl error", err);
                }, () -> {
                    LOGGER.info("Crawl command completed");
                });
    }

    @Override
    public void tearDown() throws CLIException {
        LOGGER.info("Shutting down crawl command");
        if (webPageParseSubscription != null) {
            webPageParseSubscription.dispose();
        }
        if (parentMarkSubscription != null) {
            parentMarkSubscription.dispose();
        }
        if (connection != null) {
            connection.dispose();
        }
    }
}
