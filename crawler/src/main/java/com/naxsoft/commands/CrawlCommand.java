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

    @Inject
    WebPageService webPageService = null;

    @Inject
    WebPageParserFactory webPageParserFactory = null;

    @Inject
    MetricRegistry metrics = null;

    @Override
    public void setUp(ApplicationComponent applicationComponent) throws CLIException {
        webPageService = applicationComponent.getWebPageService();
        webPageParserFactory = applicationComponent.getWebPageParserFactory();
        metrics = applicationComponent.getMetricRegistry();
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
        webPageService = null;
        webPageParserFactory = null;
        metrics = null;
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
                            .subscribe(res -> LOGGER.trace("Save {}", res), ex -> LOGGER.error("Crawler Process Exception", ex));
                    return pageToParse;
                }).subscribe(
                val -> {
                    webPageService.markParsed(val).subscribe(saveResult -> LOGGER.info("Parsed {}", val));
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
