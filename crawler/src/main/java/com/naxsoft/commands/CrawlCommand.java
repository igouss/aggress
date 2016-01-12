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

import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class CrawlCommand implements Command {
    private final static Logger logger = LoggerFactory.getLogger(CrawlCommand.class);

    private WebPageService webPageService = null;
    private WebPageParserFactory webPageParserFactory = null;
    private MetricRegistry metrics = null;

    @Override
    public void setUp(ExecutionContext context) throws CLIException {
        webPageService = context.getWebPageService();
        webPageParserFactory = context.getWebPageParserFactory();
        metrics = context.getMetrics();
    }

    @Override
    public void run() throws CLIException {
        process(webPageService.getUnparsedByType("frontPage"));
        process(webPageService.getUnparsedByType("productList"));
        process(webPageService.getUnparsedByType("productPage"));
        webPageService.getUnparsedCount("frontPage").subscribe(value -> logger.info("Unparsed frontPage = {}", value));
        webPageService.getUnparsedCount("productList").subscribe(value -> logger.info("Unparsed productList = {}", value));
        webPageService.getUnparsedCount("productPage").subscribe(value -> logger.info("Unparsed productPage = {}", value));
        logger.info("Fetch & parse complete");
    }

    @Override
    public void tearDown() throws CLIException {
        webPageService = null;
        webPageParserFactory = null;
        metrics = null;
    }

    private void process(Observable<WebPageEntity> pagesToParse) {
        pagesToParse.flatMap(pageToParse -> {
            Observable<WebPageEntity> result = null;
            try {
                WebPageParser parser = webPageParserFactory.getParser(pageToParse);
                Timer parseTime = metrics.timer(MetricRegistry.name(parser.getClass(), "parseTime"));
                Timer.Context time = parseTime.time();
                result = parser.parse(pageToParse);
                time.stop();

                pageToParse.setParsed(true);
                if (0 == webPageService.markParsed(pageToParse)) {
                    logger.error("Failed to make page as parsed {}", pageToParse);
                    result = null;
                }
            } catch (Exception e) {
                logger.error("Failed to process source {}", pageToParse.getUrl(), e);
            }
            return result;
        }).filter(webPageEntities -> null != webPageEntities)
                .retry(3)
                .subscribe(webPageService::save, ex -> logger.error("Crawler Process Exception", ex));
    }
}
