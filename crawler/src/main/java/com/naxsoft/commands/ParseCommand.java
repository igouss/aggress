package com.naxsoft.commands;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.naxsoft.ExecutionContext;
import com.naxsoft.database.Elastic;
import com.naxsoft.database.ProductService;
import com.naxsoft.database.WebPageService;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.productParser.ProductParser;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Parse raw web pages entries, generate JSON representation and sent it to Elasticsearch
 */
public class ParseCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(ParseCommand.class);
    private final Set<String> validCategories = new HashSet<>();
    private WebPageService webPageService = null;
    private ProductService productService = null;
    private Elastic elastic = null;
    private ProductParserFactory productParserFactory = null;
    private MetricRegistry metrics = null;
    private String indexSuffix = null;
    private WebPageParserFactory webPageParserFactory;

    public ParseCommand() {
        validCategories.add("n/a");
        validCategories.add("firearms");
        validCategories.add("reloading");
        validCategories.add("ammo");
        validCategories.add("misc");
    }

    @Override
    public void setUp(ExecutionContext context) throws CLIException {
        webPageService = context.getWebPageService();
        productService = context.getProductService();
        elastic = context.getElastic();
        productParserFactory = context.getProductParserFactory();
        webPageParserFactory = context.getWebPageParserFactory();
        metrics = context.getMetrics();
        indexSuffix = context.getIndexSuffix();
    }

    @Override
    public void run() throws CLIException {
        Observable<ProductEntity> products = webPageService.getUnparsedByType("productPage").flatMap(webPageEntity -> {
            WebPageParser parser = webPageParserFactory.getParser(webPageEntity);
            webPageService.markParsed(webPageEntity);
            return parser.parse(webPageEntity);
        }).map(pageToParse -> {
            Set<ProductEntity> result = null;
            try {
                ProductParser parser = productParserFactory.getParser(pageToParse);
                Timer parseTime = metrics.timer(MetricRegistry.name(parser.getClass(), "parseTime"));
                Timer.Context time = parseTime.time();
                result = parser.parse(pageToParse);
                time.stop();
            } catch (Exception e) {
                LOGGER.error("Failed to parse product page {}", pageToParse.getUrl(), e);
            }
            return result;
        }).filter(webPageEntities -> null != webPageEntities)
                .flatMap(Observable::from);
        indexProducts(products, "product" + indexSuffix, "guns");


        LOGGER.info("Parsing complete");
    }

    @Override
    public void tearDown() throws CLIException {
        webPageService = null;
        productService = null;
        elastic = null;
        productParserFactory = null;
        webPageParserFactory = null;
        metrics = null;
        indexSuffix = null;
    }

    /**
     * Adds stream of products to Elasticsearch
     *
     * @param products Stream of products to save
     * @param index    Elasticsearch index
     * @param type     Elasticsearch type
     * @return
     */
    private Subscription indexProducts(Observable<ProductEntity> products, String index, String type) {
        LOGGER.info("Indexing products");
        return elastic.index(products, index, type);
    }


}
