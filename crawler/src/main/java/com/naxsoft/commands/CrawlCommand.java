package com.naxsoft.commands;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.lambdaworks.redis.event.EventBus;
import com.naxsoft.ApplicationComponent;
import com.naxsoft.database.WebPageService;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.events.WebPageEntityParseEvent;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
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
    private EventBus eventBus;

    @Override
    public void setUp(ApplicationComponent applicationComponent) throws CLIException {
        webPageService = applicationComponent.getWebPageService();
        webPageParserFactory = applicationComponent.getWebPageParserFactory();
        metrics = applicationComponent.getMetricRegistry();
        eventBus = applicationComponent.getEventBus();
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
     * Process the stream of unparsed webpages. Processed web pages are saved into the database.
     *
     * @param pagesToParse Stream of webpages to process
     */
    private void process(Observable<WebPageEntity> pagesToParse) {
        Observable<WebPageEntity> parsedWebPageEntries = pagesToParse
                .observeOn(Schedulers.computation())
                .flatMap(pageToParse -> {
                    Observable<WebPageEntity> result = null;

                    eventBus.publish(new WebPageEntityParseEvent(pageToParse));

                    WebPageParser parser = webPageParserFactory.getParser(pageToParse);
                    Timer parseTime = metrics.timer(MetricRegistry.name(parser.getClass(), "parseTime"));
                    Timer.Context time = parseTime.time();
                    try {
                        result = parser.parse(pageToParse);
                    } catch (Exception e) {
                        LOGGER.error("Failed to parse source {}", pageToParse.getUrl(), e);
                    } finally {
                        time.stop();
                    }

                    webPageService.markParsed(pageToParse)
                            .subscribe(value -> {
                                LOGGER.debug("Page parsed {}", value);
                            }, error -> {
                                LOGGER.error("Failed to make page as parsed", error);
                            });

                    return result;
                });
        Semaphore semaphore = new Semaphore(0);

        parsedWebPageEntries
                .filter(webPageEntities -> null != webPageEntities)
                .flatMap(webPageService::save)
                .subscribe(
                        result -> {
                        },
                        ex -> {
                            LOGGER.error("Crawler Process Exception", ex);
                        },
                        semaphore::release
                );

        try {
            semaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
