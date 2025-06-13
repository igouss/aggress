package com.naxsoft.commands;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsingService.WebPageService;
import com.naxsoft.storage.elasticsearch.Elastic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Parse raw web pages entries, generate JSON representation and sent it to Elasticsearch
 */
@Component
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
    private Disposable productIndexSubscription;
    private Disposable priceIndexSubscription;

    @Autowired
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
        Flux<ProductEntity> productPages = Flux.interval(Duration.ofSeconds(5))
                .flatMap(i -> webPageService.getUnparsedByType("productPageRaw"))
                .doOnNext(webPageEntity -> LOGGER.info("Starting RAW page parsing {}", webPageEntity))
                .flatMap(productParserFactory::parse)
                .doOnNext(productEntity -> LOGGER.info("Parsed page {}", productEntity))
                .share();

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
            productIndexSubscription.dispose();
        }
        if (priceIndexSubscription != null) {
            priceIndexSubscription.dispose();
        }
    }
}
