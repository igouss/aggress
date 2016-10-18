package com.naxsoft.commands;

import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsingService.WebPageService;
import com.naxsoft.storage.elasticsearch.Elastic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    @Inject
    public ParseCommand(WebPageService webPageService, ProductParserFactory productParserFactory, Elastic elastic) {
        this.webPageService = webPageService;
        this.productParserFactory = productParserFactory;
        this.elastic = elastic;
    }

    @Override
    public void setUp() throws CLIException {

    }

    @Override
    public void start() throws CLIException {
        webPageService.getUnparsedByType("productPageRaw", 5, TimeUnit.SECONDS)
                .doOnNext(webPageEntity -> LOGGER.info("Starting RAW page parsing {}", webPageEntity))
                .flatMap(productParserFactory::parse)
                .doOnNext(productEntity -> LOGGER.info("Starting indexing {}", productEntity))
                .flatMap(product -> elastic.index(product, "product", "guns"))
                .subscribe(
                        val -> {
//                            LOGGER.info("Indexed: {}", val);
                        },
                        err -> LOGGER.error("Failed", err),
                        () -> LOGGER.info("Completed")
                );
        LOGGER.info("Parsing complete");
    }


    @Override
    public void tearDown() throws CLIException {
    }
}
