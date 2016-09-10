package com.naxsoft.commands;

import com.naxsoft.database.Elastic;
import com.naxsoft.database.WebPageService;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.utils.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

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
    private String indexSuffix = null;

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
        process(webPageService.getUnparsedByType("productPageRaw", 5, TimeUnit.SECONDS), "product", indexSuffix, "guns");
        LOGGER.info("Parsing complete");
    }


    @Override
    public void tearDown() throws CLIException {
    }

    /**
     * Adds stream of products to Elasticsearch
     */
    private void process(Observable<WebPageEntity> productPagesRaw, String index, String indexSuffix, String type) {
        productPagesRaw
                .flatMap(productPageRaw -> Observable.zip(Observable.just(productParserFactory.parse(productPageRaw)), webPageService.markParsed(productPageRaw), Tuple::new))
                .flatMap(tuple2 -> elastic.index(tuple2.getV1(), index, indexSuffix, type))
                .subscribe(
                        val -> {
                        },
                        err -> LOGGER.error("Failed", err),
                        () -> LOGGER.info("Completed")
                );
    }
}
