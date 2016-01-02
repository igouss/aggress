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

    private WebPageService webPageService;
    private WebPageParserFactory webPageParserFactory;
    private MetricRegistry metrics;

    @Override
    public void setUp(ExecutionContext context) throws CLIException {
        webPageService = context.getWebPageService();
        webPageParserFactory = context.getWebPageParserFactory();
        metrics = context.getMetrics();
    }

    @Override
    public void run() throws CLIException {
        process(webPageService.getUnparsedFrontPage());
        process(webPageService.getUnparsedProductList());
        process(webPageService.getUnparsedProductPage());
        logger.info("Fetch & parse complete");

    }

    @Override
    public void tearDown() throws CLIException {
        webPageService = null;
        webPageParserFactory = null;
        metrics = null;
    }

    private void process(Observable<WebPageEntity> parents) {
        parents.flatMap(parent -> {
            WebPageParser parser = webPageParserFactory.getParser(parent);
            Timer parseTime = metrics.timer(MetricRegistry.name(parser.getClass(), "parseTime"));
            Timer.Context time = parseTime.time();
            Observable<Set<WebPageEntity>> result = null;
            try {
                result = parser.parse(parent);
            } catch (Exception e) {
                logger.error("Failed to process source {}", parent.getUrl(), e);
            }
            time.stop();
            webPageService.markParsed(parent);
            return result;
        }).filter(webPageEntities -> null != webPageEntities)
                .map(webPageService::save)
                .retry(3)
                .doOnError(ex -> logger.error("Exception", ex))
                .subscribe();
    }

}
