package com.naxsoft.commands;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.naxsoft.ExecutionContext;
import com.naxsoft.database.WebPageService;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.concurrent.Semaphore;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Crawl pages from initial dataset walking breath first. For each page generate one or more sub-pages to parse.
 * Stop at leafs.
 */
public class CrawlCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(CrawlCommand.class);

    private WebPageService webPageService = null;
    private WebPageParserFactory webPageParserFactory = null;
    private MetricRegistry metrics = null;

    @Override
    public void setUp(ExecutionContext context) throws CLIException {
        webPageService = context.getWebPageService();
        webPageParserFactory = new WebPageParserFactory(context.getHTTPClient());
        metrics = context.getMetrics();
    }

    @Override
    public void run() throws CLIException {
        process(webPageService.getUnparsedByType("frontPage"));
        process(webPageService.getUnparsedByType("productList"));
        process(webPageService.getUnparsedByType("productPage"));
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
     * Process the stream of unparsed webpages. Processed web pages are saved into the database.
     *
     * @param pagesToParse Stream of webpages to process
     */
    private void process(Observable<WebPageEntity> pagesToParse) {
        pagesToParse
                .flatMap(pageToParse -> {
                    Observable<WebPageEntity> result = null;
                    try {
                        WebPageParser parser = webPageParserFactory.getParser(pageToParse);
                        Timer parseTime = metrics.timer(MetricRegistry.name(parser.getClass(), "parseTime"));
                        Timer.Context time = parseTime.time();
                        result = parser.parse(pageToParse);
                        time.stop();

                        pageToParse.setParsed(true);
                        if (0 == webPageService.markParsed(pageToParse)) {
                            LOGGER.error("Failed to make page as parsed {}", pageToParse);
                            result = null;
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to process source {}", pageToParse.getUrl(), e);
                    }
                    return result;
                }).filter(webPageEntities -> null != webPageEntities)
                .subscribe(
                        webPageService::save,
                        ex -> LOGGER.error("Crawler Process Exception", ex)
                );
    }
}
