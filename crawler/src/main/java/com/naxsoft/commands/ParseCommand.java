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

import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class ParseCommand implements Command {
    private final static Logger logger = LoggerFactory.getLogger(ParseCommand.class);
    private WebPageService webPageService;
    private ProductService productService;
    private Elastic elastic;
    private ProductParserFactory productParserFactory;
    private MetricRegistry metrics;
    private String indexSuffix;

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
        processProducts(webPageService.getUnparsedProductPageRaw());
        indexProducts(productService.getProducts(), "product" + indexSuffix, "guns");
        productService.markAllAsIndexed();
        logger.info("Parsing complete");
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

    private void indexProducts(Observable<ProductEntity> products, String index, String type) {
        elastic.index(products, index, type);
    }

    private void processProducts(Observable<WebPageEntity> webPage) {
        webPage.map(webPageEntity -> {
            Set<ProductEntity> result = null;
            try {
                ProductParser parser = productParserFactory.getParser(webPageEntity);
                Timer parseTime = metrics.timer(MetricRegistry.name(parser.getClass(), "parseTime"));
                Timer.Context time = parseTime.time();
                result = parser.parse(webPageEntity);
                time.stop();
                webPageService.markParsed(webPageEntity);
            } catch (Exception e) {
                logger.error("Failed to parse product page {}", webPageEntity.getUrl(), e);
            }
            return result;
        }).filter(webPageEntities -> null != webPageEntities)
                .retry(3)
                .doOnError(ex -> logger.error("Exception", ex))
                .subscribe(productService::save);
    }
}
