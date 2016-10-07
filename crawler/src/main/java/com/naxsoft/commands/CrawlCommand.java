package com.naxsoft.commands;

import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.parsingService.WebPageService;
import com.naxsoft.utils.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * Copyright NAXSoft 2015
 *
 * Crawl pages from initial data-set walking breath first. For each page generate one or more sub-pages to parse.
 * Stop at leafs.
 * Process the stream of unparsed webpages. Processed web pages are saved into the
 * database and the page is marked as parsed
 */
public class CrawlCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(CrawlCommand.class);

    private final WebPageService webPageService;
    private final WebPageParserFactory webPageParserFactory;

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
        Observable.merge(
                webPageService.getUnparsedByType("frontPage", 5, TimeUnit.SECONDS),
                webPageService.getUnparsedByType("productList", 5, TimeUnit.SECONDS),
                webPageService.getUnparsedByType("productPage", 5, TimeUnit.SECONDS))
                .flatMap(pageToParse -> Observable.zip(Observable.just(webPageParserFactory.parse(pageToParse)), webPageService.markParsed(pageToParse), Tuple::new))
                .flatMap(tuple -> webPageService.addWebPageEntry(tuple.getV1()))
                .subscribe(
                        rc -> {
                        },
                        err -> LOGGER.error("Failed", err),
                        () -> LOGGER.info("Crawl completed")
                );
    }

    @Override
    public void tearDown() throws CLIException {
    }
}
