package com.naxsoft.commands;

import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsingService.WebPageService;
import com.naxsoft.storage.elasticsearch.Elastic;
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
 * Parse raw web pages entries, generate JSON representation and sent it to Elasticsearch
 */
public class ParseCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(ParseCommand.class);

    private WebPageService webPageService;
    private Elastic elastic;
    private ProductParserFactory productParserFactory;
    private Disposable productIndexSubscription;
    private Disposable priceIndexSubscription;
    private Disposable connection;

    @Inject
    public ParseCommand(WebPageService webPageService, ProductParserFactory productParserFactory, Elastic elastic) {
        this.webPageService = webPageService;
        this.productParserFactory = productParserFactory;
        this.elastic = elastic;
        priceIndexSubscription = null;
        productIndexSubscription = null;
    }

    @Override
    public void setUp() throws CLIException {

    }

    @Override
    public void start() throws CLIException {
        connection = Flowable.interval(5, TimeUnit.SECONDS, Schedulers.io())
                .onBackpressureDrop()
                .flatMap(i -> webPageService.getUnparsedByType("productPageRaw"))
                .doOnNext(webPageEntity -> LOGGER.info("Starting RAW page parsing {}", webPageEntity))
                .flatMap(productParserFactory::parse)
                .doOnNext(productEntity -> LOGGER.info("Starting product indexing {}", productEntity))
                .buffer(1, TimeUnit.SECONDS).map(product ->
                        elastic.index(product, "product", "guns") && elastic.price_index(product, "product", "prices"))
                .onBackpressureBuffer()
                .subscribe(
                        val -> LOGGER.info("Indexed: {}", val),
                        err -> LOGGER.error("Product indexing failed", err),
                        () -> LOGGER.info("Product indexing complete")
                );

    }


    @Override
    public void tearDown() throws CLIException {
        if (connection != null) {
            connection.dispose();
        }
        if (productIndexSubscription != null) {
            productIndexSubscription.dispose();
        }
        if (priceIndexSubscription != null) {
            priceIndexSubscription.dispose();
        }
    }
}
