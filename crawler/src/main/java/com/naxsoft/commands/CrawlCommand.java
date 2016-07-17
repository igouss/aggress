package com.naxsoft.commands;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.ApplicationComponent;
import com.naxsoft.database.WebPageService;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import java.util.concurrent.Semaphore;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Crawl pages from initial dataset walking breath first. For each page generate one or more sub-pages to parse.
 * Stop at leafs.
 */
public class CrawlCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(CrawlCommand.class);

    private WebPageService webPageService;
    private WebPageParserFactory webPageParserFactory;

    @Inject
    public CrawlCommand(WebPageService webPageService, WebPageParserFactory webPageParserFactory) {
        this.webPageService = webPageService;
        this.webPageParserFactory = webPageParserFactory;
    }

    @Override
    public void setUp() throws CLIException {
    }

    @Override
    public void start() throws CLIException {
        process(webPageService.getUnparsedByType("frontPage"));
        process(webPageService.getUnparsedByType("productList"));

//        webPageService.getUnparsedCount("frontPage").take(1).subscribe(value -> {
//            LOGGER.info("Unparsed frontPage = {}", value);
//        });
//        webPageService.getUnparsedCount("productList").take(1).subscribe(value -> {
//            LOGGER.info("Unparsed productList = {}", value);
//        });
//        webPageService.getUnparsedCount("productPage").take(1).subscribe(value -> {
//            LOGGER.info("Unparsed productPage = {}", value);
//        });
        LOGGER.info("Fetch & parse complete");
    }

    @Override
    public void tearDown() throws CLIException {
    }

    /**
     * Process the stream of unparsed webpages. Processed web pages are saved into the
     * database and the page is marked as parsed
     *
     * @param pagesToParse Stream of webpages to process
     */
    private void process(Observable<WebPageEntity> pagesToParse) {
        Semaphore processCompleteSemaphore = new Semaphore(0);

        pagesToParse
                .observeOn(Schedulers.computation())
                .map(pageToParse -> {
                    webPageParserFactory.parse(pageToParse)
                            .filter(parseResult -> null != parseResult)
                            .flatMap(webPageService::save)
                            .subscribe(
                                    res -> LOGGER.trace("Save {}", res),
                                    ex -> LOGGER.error("Crawler Process Exception", ex),
                                    () -> LOGGER.info("Save completed")
                            );
                    return pageToParse;
                }).subscribe(
                val -> {
                    webPageService.markParsed(val).subscribe(
                            saveResult -> LOGGER.info("Marking as parsed {} {}", val, saveResult),
                            err -> LOGGER.error("Failed to mark as parsed", err),
                            () -> LOGGER.info("Mark as parsed completed")
                    );
                },
                err -> LOGGER.error("Failed to crawl", err),
                processCompleteSemaphore::release);
        try {
            processCompleteSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
