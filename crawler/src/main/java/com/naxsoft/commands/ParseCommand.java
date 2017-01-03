package com.naxsoft.commands;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsingService.WebPageService;
import com.naxsoft.storage.elasticsearch.Elastic;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Parse raw web pages entries, generate JSON representation and sent it to Elasticsearch
 */
public class ParseCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(ParseCommand.class);

    private final Vertx vertx;
    private WebPageService webPageService;
    private Elastic elastic;
    private ProductParserFactory productParserFactory;

    private Disposable productPageRawDisposable;
    private Disposable priceIndexDisposable;

    @Inject
    public ParseCommand(Vertx vertx, WebPageService webPageService, ProductParserFactory productParserFactory, Elastic elastic) {
        this.vertx = vertx;
        this.webPageService = webPageService;
        this.productParserFactory = productParserFactory;
        this.elastic = elastic;
        priceIndexDisposable = null;
        productPageRawDisposable = null;
    }

    @Override
    public void setUp() throws CLIException {

    }

    @Override
    public void start() throws CLIException {
        MessageConsumer<ProductEntity> consumer = vertx.eventBus().consumer("productParseResult");
        consumer.handler(message -> {
            LOGGER.trace("Message received {} {}", message.address(), message.body());
            ProductEntity productEntity = message.body();
            if (productEntity != null) {
                elastic.index(productEntity, "product", "guns")
                        .subscribe(
                                rc -> LOGGER.info("Index op={}", rc),
                                err -> LOGGER.error("Index failed", err),
                                () -> LOGGER.info("Index complete")
                        );
            } else {
                LOGGER.error("Unexpected product", new Exception("NULL ProductEntity"));
            }
        });
        consumer.exceptionHandler(error -> {
            LOGGER.error("Error received", error);
        });


        productPageRawDisposable = Flowable
                .interval(5, TimeUnit.SECONDS, Schedulers.io())
                .observeOn(Schedulers.io())
                .onBackpressureDrop()
                .flatMap(i -> webPageService.getUnparsedByType("productPageRaw"))
                .doOnNext(webPageEntity -> LOGGER.trace("productPageRaw: Starting RAW page parsing {}", webPageEntity))
                .doOnNext(productParserFactory::parse)
                .subscribe(
                        val -> LOGGER.trace("productIndex: Indexed: {}", val),
                        err -> LOGGER.error("productIndex: Product indexing failed", err),
                        () -> LOGGER.trace("productIndex: Product indexing complete")
                );
    }


    @Override
    public void tearDown() throws CLIException {
        LOGGER.trace("Shutting down ParseCommand");
        if (productPageRawDisposable != null && !productPageRawDisposable.isDisposed()) {
            productPageRawDisposable.dispose();
        }

        if (priceIndexDisposable != null && !priceIndexDisposable.isDisposed()) {
            priceIndexDisposable.dispose();
        }
    }
}
