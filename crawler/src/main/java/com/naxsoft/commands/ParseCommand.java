package com.naxsoft.commands;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsingService.WebPageService;
import com.naxsoft.storage.elasticsearch.Elastic;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Parse raw web pages entries, generate JSON representation and sent it to Elasticsearch
 */
public class ParseCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(ParseCommand.class);

    private final WebPageService webPageService;
    private final Elastic elastic;
    private final ProductParserFactory productParserFactory;

    private Disposable productPageRawDisposable;
    private Disposable productIndexDisposable;
    private Disposable priceIndexDisposable;

    @Inject
    public ParseCommand(WebPageService webPageService, ProductParserFactory productParserFactory, Elastic elastic) {
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

        Flowable<List<ProductEntity>> productPageRaw = Flowable.interval(5, TimeUnit.SECONDS, Schedulers.io())
                .observeOn(Schedulers.io())
                .onBackpressureDrop()
                .flatMap(i -> webPageService.getUnparsedByType("productPageRaw"))
                .doOnNext(webPageEntity -> LOGGER.info("Starting RAW page parsing {}", webPageEntity))
                .flatMap(productParserFactory::parse)
                .doOnNext(productEntity -> LOGGER.info("Starting product indexing {}", productEntity))
                .buffer(1, TimeUnit.SECONDS)
                .onBackpressureBuffer()
                .filter(productEntities -> !productEntities.isEmpty())
                .publish().autoConnect(2, con -> productPageRawDisposable = con);

        productIndexDisposable = productPageRaw.flatMap(product -> elastic.index(product, "product", "guns"))
                .subscribe(
                        val -> LOGGER.info("Indexed: {}", val),
                        err -> LOGGER.error("Product indexing failed", err),
                        () -> LOGGER.info("Product indexing complete")
                );

        priceIndexDisposable = productPageRaw.flatMap(product -> elastic.price_index(product, "product", "prices"))
                .subscribe(
                        val -> LOGGER.info("Price indexed: {}", val),
                        err -> LOGGER.error("Price indexing failed", err),
                        () -> LOGGER.info("Price indexing complete")
                );
    }


    @Override
    public void tearDown() throws CLIException {
        LOGGER.info("Shutting down ParseCommand");
        if (productPageRawDisposable != null && !productPageRawDisposable.isDisposed()) {
            productPageRawDisposable.dispose();
        }

        if (productIndexDisposable != null && !productIndexDisposable.isDisposed()) {
            productIndexDisposable.dispose();
        }

        if (priceIndexDisposable != null && !priceIndexDisposable.isDisposed()) {
            priceIndexDisposable.dispose();
        }
    }
}
