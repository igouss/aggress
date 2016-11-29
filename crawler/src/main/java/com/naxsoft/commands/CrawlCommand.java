package com.naxsoft.commands;

import com.naxsoft.entity.WebPageEntity;
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

    private Disposable parentMarkDisposable;
    private Disposable parserDisposable;
    private Disposable listFlowableDisposable;

    @Inject
    public CrawlCommand(WebPageService webPageService, WebPageParserFactory webPageParserFactory) {
        this.webPageService = webPageService;
        this.webPageParserFactory = webPageParserFactory;

        parentMarkDisposable = null;
        parserDisposable = null;
    }

    @Override
    public void setUp() throws CLIException {
    }

    @Override
    public void start() throws CLIException {
        Flowable<WebPageEntity> listFlowable = Flowable.interval(0, 5, TimeUnit.SECONDS)
                .onBackpressureDrop()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .flatMap(i -> Flowable.mergeDelayError(
                        webPageService.getUnparsedByType("frontPage"),
                        webPageService.getUnparsedByType("productList"),
                        webPageService.getUnparsedByType("productPage")))
                .doOnNext(val -> LOGGER.info("listFlowable: {}", val))
                .publish().autoConnect(2, val -> listFlowableDisposable = val);

        parentMarkDisposable = listFlowable
                .onBackpressureBuffer()
                .doOnCancel(() -> LOGGER.warn("parentMarkDisposable: parse cancel called"))
                .doOnError(throwable -> LOGGER.error("parentMarkDisposable: Error", throwable))
                .doOnNext(webPageEntity -> LOGGER.info("parentMarkDisposable: Starting parse {}", webPageEntity))
                .observeOn(Schedulers.io())
                .buffer(1, TimeUnit.SECONDS)
                .onBackpressureBuffer()
                .filter(webPageEntities -> !webPageEntities.isEmpty())
                .flatMap(webPageService::markParsed)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        val -> LOGGER.info("parentMarkDisposable: Added new page {}", val),
                        err -> LOGGER.error("parentMarkDisposable: Crawl error", err),
                        () -> LOGGER.info("parentMarkDisposable: Crawl command completed"));

        parserDisposable = listFlowable
                .onBackpressureBuffer()
                .doOnCancel(() -> LOGGER.warn("parserDisposable: parse cancel called"))
                .doOnError(throwable -> LOGGER.error("parserDisposable: Error", throwable))
                .doOnNext(webPageEntity -> LOGGER.info("parserDisposable: Starting parse {}", webPageEntity))
                .observeOn(Schedulers.io())
                .buffer(1, TimeUnit.SECONDS)
                .onBackpressureBuffer()
                .filter(webPageEntities -> !webPageEntities.isEmpty())
                .observeOn(Schedulers.io())
                .flatMap(webPageParserFactory::parse)
                .buffer(1, TimeUnit.SECONDS)
                .filter(webPageEntities -> !webPageEntities.isEmpty())
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .flatMap(webPageService::addWebPageEntry)
                .subscribeOn(Schedulers.io())
                .subscribe(
                        val -> LOGGER.info("parserDisposable: Added new page {}", val),
                        err -> LOGGER.error("parserDisposable: Crawl error", err),
                        () -> LOGGER.info("parserDisposable: Crawl command completed"));
    }

    @Override
    public void tearDown() throws CLIException {
        LOGGER.info("Shutting down crawl command");

        if (parentMarkDisposable != null && !parentMarkDisposable.isDisposed()) {
            parentMarkDisposable.dispose();
        }
        if (parserDisposable != null && !parserDisposable.isDisposed()) {
            parserDisposable.dispose();
        }
        if (listFlowableDisposable != null && !listFlowableDisposable.isDisposed()) {
            listFlowableDisposable.dispose();
        }
    }
}
