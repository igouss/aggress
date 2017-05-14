package com.naxsoft.commands;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsingService.WebPageService;
import com.naxsoft.storage.elasticsearch.Elastic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Parse raw web pages entries, generate JSON representation and sent it to Elasticsearch
 */
public class ParseCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(ParseCommand.class);

    private final static Set<String> VALID_CATEGORIES = new HashSet<>();

    static {
        VALID_CATEGORIES.add("firearm");
        VALID_CATEGORIES.add("reload");
        VALID_CATEGORIES.add("optic");
        VALID_CATEGORIES.add("ammo");
        VALID_CATEGORIES.add("misc");
    }

    private WebPageService webPageService = null;
    private Elastic elastic = null;
    private ProductParserFactory productParserFactory = null;
    private Subscription productIndexSubscription;
    private Subscription priceIndexSubscription;

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
        Observable<ProductEntity> productPages = Observable.interval(5, TimeUnit.SECONDS)
                .flatMap(i -> webPageService.getUnparsedByType("productPageRaw"))
                .doOnNext(webPageEntity -> LOGGER.info("Starting RAW page parsing {}", webPageEntity))
                .flatMap(productParserFactory::parse)
                .doOnNext(productEntity -> LOGGER.info("Parsed page {}", productEntity))
                .publish()
                .autoConnect(2);

        productIndexSubscription = productPages
                .doOnNext(productEntity -> LOGGER.info("Starting product indexing {}", productEntity))
                .buffer(16)
                .flatMap(product -> elastic.index(product, "product", "guns"))
                .subscribe(
                        val -> {
                            LOGGER.info("Indexed: {}", val);
                        },
                        err -> LOGGER.error("Product indexing failed", err),
                        () -> LOGGER.info("Product indexing completed")
                );

        priceIndexSubscription = productPages
                .doOnNext(productEntity -> LOGGER.info("Starting price indexing {}", productEntity))
                .buffer(16)
                .flatMap(product -> elastic.price_index(product, "product", "prices"))
                .subscribe(
                        val -> {
                            LOGGER.info("Price indexed: {}", val);
                        },
                        err -> LOGGER.error("Price indexing failed", err),
                        () -> LOGGER.info("Price indexing completed")
                );
    }


    @Override
    public void tearDown() throws CLIException {
        if (productIndexSubscription != null) {
            productIndexSubscription.unsubscribe();
        }
        if (priceIndexSubscription != null) {
            priceIndexSubscription.unsubscribe();
        }
        productParserFactory.close();
    }
}
