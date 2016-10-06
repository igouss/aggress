package com.naxsoft.commands;

import com.naxsoft.entity.WebPageEntity;
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
        process(webPageService.getUnparsedByType("frontPage", 5, TimeUnit.SECONDS), "frontPage");
        process(webPageService.getUnparsedByType("productList", 5, TimeUnit.SECONDS), "productList");
        process(webPageService.getUnparsedByType("productPage", 5, TimeUnit.SECONDS), "productPage");
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
    private void process(Observable<WebPageEntity> pagesToParse, final String type) {
        pagesToParse
                .flatMap(pageToParse -> Observable.zip(Observable.just(webPageParserFactory.parse(pageToParse)), webPageService.markParsed(pageToParse), Tuple::new))
                .flatMap(tuple -> webPageService.addWebPageEntry(tuple.getV1()))
                .subscribe(
                        rc -> {
                        },
                        err -> LOGGER.error("Failed", err),
                        () -> LOGGER.info("Completed {}", type)
                );
    }
}
