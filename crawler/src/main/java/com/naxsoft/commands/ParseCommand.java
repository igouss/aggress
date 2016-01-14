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

import java.security.InvalidParameterException;
import java.util.InvalidPropertiesFormatException;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class ParseCommand implements Command {
    private final static Logger logger = LoggerFactory.getLogger(ParseCommand.class);
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

    private void processProducts(Observable<WebPageEntity> pagesToParse) {
        pagesToParse.map(pageToParse -> {
            Set<ProductEntity> result = null;
            try {
                ProductParser parser = productParserFactory.getParser(pageToParse);
                Timer parseTime = metrics.timer(MetricRegistry.name(parser.getClass(), "parseTime"));
                Timer.Context time = parseTime.time();


                String webPageEntityCategory = pageToParse.getCategory();
                if (null == webPageEntityCategory || webPageEntityCategory.isEmpty()) {
                    throw new InvalidPropertiesFormatException("Category not set");
                }

                if (!pageToParse.getCategory().toLowerCase().equals("N/A") || !pageToParse.getCategory().toLowerCase().equals("Firearms") || !pageToParse.getCategory().toLowerCase().equals("Ammo") || !pageToParse.getCategory().toLowerCase().equals("Misc")) {
                    throw new InvalidParameterException("Invalid category name");
                }

                result = parser.parse(pageToParse);
                time.stop();

                pageToParse.setParsed(true);
                if (0 == webPageService.markParsed(pageToParse)) {
                    logger.error("Failed to make page as parsed {}", pageToParse);
                    result = null;
                }
            } catch (Exception e) {
                logger.error("Failed to parse product page {}", pageToParse.getUrl(), e);
            }
            return result;
        }).filter(webPageEntities -> null != webPageEntities)
                .retry(3)
                .subscribe(productService::save, ex -> logger.error("Parser Process Exception", ex));
    }
}
