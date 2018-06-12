package com.naxsoft.commands;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.parsingService.WebPageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
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

    @Inject
    public CrawlCommand(WebPageService webPageService, WebPageParserFactory webPageParserFactory) {
        this.webPageService = webPageService;
        this.webPageParserFactory = webPageParserFactory;
        webPageParseSubscription = null;
    }

    @Override
    public void setUp() throws CLIException {
    }

    @Override
    public void start() throws CLIException {
        Set<WebPageEntity> pagesToParse = new HashSet<>();
        pagesToParse.addAll(webPageService.getUnparsedByType("frontPage"));
        pagesToParse.addAll(webPageService.getUnparsedByType("productList"));
        pagesToParse.addAll(webPageService.getUnparsedByType("productPage"));

        webPageParseSubscription = Observable.from(pagesToParse)
                .doOnNext(webPageEntity -> LOGGER.info("Starting parse {}", webPageEntity))
                .map(webPageParserFactory::parse)
                .flatMap(Observable::from)
                .map(page -> {
                    webPageService.markParsed(page);
                    webPageService.addWebPageEntry(page);
                    return true;
                })
                .subscribe(
                        rc -> LOGGER.trace("Added WebPageEntry, parent marked as parsed: {} results added to DB"),
                        err -> LOGGER.error("Failed", err),
                        () -> LOGGER.info("Crawl completed")
                );
    }

    @Override
    public void tearDown() throws CLIException {
        if (webPageParseSubscription != null) {
            webPageParseSubscription.unsubscribe();
        }
    }
}
