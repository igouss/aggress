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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;

import java.security.InvalidParameterException;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 *
 * Parse raw web pages entries, generate JSON representation and sent it to Elasticsearch
 *
 */
public class ParseCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(ParseCommand.class);
    private WebPageService webPageService = null;
    private ProductService productService = null;
    private Elastic elastic = null;
    private ProductParserFactory productParserFactory = null;
    private MetricRegistry metrics = null;
    private String indexSuffix = null;

    @Override
    public void setUp(ExecutionContext context) throws CLIException {
        webPageService = context.getWebPageService();
        productService = context.getProductService();
        elastic = context.getElastic();
        productParserFactory = context.getProductParserFactory();
        metrics = context.getMetrics();
        indexSuffix = context.getIndexSuffix();
    }

    @Override
    public void run() throws CLIException {
        processProducts(webPageService.getUnparsedByType("productPageRaw"));
        indexProducts(productService.getProducts(), "product" + indexSuffix, "guns");
        productService.markAllAsIndexed();
        LOGGER.info("Parsing complete");
    }

    @Override
    public void tearDown() throws CLIException {
        webPageService = null;
        productService = null;
        elastic = null;
        productParserFactory = null;
        metrics = null;
        indexSuffix = null;
    }

    /**
     *
     * @param products
     * @param index
     * @param type
     * @return
     */
    private Subscription indexProducts(Observable<ProductEntity> products, String index, String type) {
        return elastic.index(products, index, type);
    }

    /**
     *
     * @param pagesToParse
     */
    private void processProducts(Observable<WebPageEntity> pagesToParse) {
        pagesToParse.map(pageToParse -> {
            Set<ProductEntity> result = null;
            try {
                ProductParser parser = productParserFactory.getParser(pageToParse);
                Timer parseTime = metrics.timer(MetricRegistry.name(parser.getClass(), "parseTime"));
                Timer.Context time = parseTime.time();


                String webPageEntityCategory = pageToParse.getCategory();
                // Check is category is set and has valid name
                if (null != webPageEntityCategory && !webPageEntityCategory.isEmpty()) {
                    if (pageToParse.getCategory().toLowerCase().equals("N/A") && pageToParse.getCategory().toLowerCase().equals("Firearms") && pageToParse.getCategory().toLowerCase().equals("Ammo") && pageToParse.getCategory().toLowerCase().equals("Misc")) {
                        result = parser.parse(pageToParse);
                        time.stop();
                        if (null != result) {
                            pageToParse.setParsed(true);
                            if (0 == webPageService.markParsed(pageToParse)) {
                                LOGGER.error("Failed to make page as parsed {}", pageToParse);
                                result = null;
                            }
                        } else {
                            LOGGER.error("failed to parse {}", pageToParse.getUrl());
                        }
                    } else {
                        throw new InvalidParameterException("Invalid category name");
                    }
                } else {
                    throw new InvalidParameterException("Category not set");
                }
            } catch (Exception e) {
                LOGGER.error("Failed to parse product page {}", pageToParse.getUrl(), e);
            }
            return result;
        }).filter(webPageEntities -> null != webPageEntities)
                .retry(3)
                .subscribe(productService::save, ex -> LOGGER.error("Parser Process Exception", ex));
    }
}
