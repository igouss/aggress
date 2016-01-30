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

    public ParseCommand() {
        validCategories.add("n/a");
        validCategories.add("firearms");
        validCategories.add("ammo");
        validCategories.add("misc");
    }

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
        Observable<WebPageEntity> productPageRaw = webPageService.getUnparsedByType("productPageRaw");
        processProducts(productPageRaw);
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
     * Adds stream of products to Elasticsearch
     * @param products Stream of products to save
     * @param index Elasticsearch index
     * @param type Elasticsearch type
     * @return
     */
    private Subscription indexProducts(Observable<ProductEntity> products, String index, String type) {
        return elastic.index(products, index, type);
    }

    /**
     * Parse stream of webpages and save product entries into the database
     * @param pagesToParse Stream of pages to parse
     */
    private void processProducts(Observable<WebPageEntity> pagesToParse) {
        pagesToParse.map(pageToParse -> {
            Set<ProductEntity> result = null;
            try {
                ProductParser parser = productParserFactory.getParser(pageToParse);
                Timer parseTime = metrics.timer(MetricRegistry.name(parser.getClass(), "parseTime"));
                Timer.Context time = parseTime.time();

                String allCategories = pageToParse.getCategory();
                if (null != allCategories) {
                    String[] categories = allCategories.split(",");
                    for (String category : categories) {
                        if (!validCategories.contains(category)) {
                            LOGGER.error("Invalid category: {}, entry {}", category, pageToParse);
                        }
                    }
                } else {
                    LOGGER.info("Category not set {}", pageToParse);
                }

                result = parser.parse(pageToParse);
                time.stop();
                if (null != result) {
                    pageToParse.setParsed(true);
                    if (0 == webPageService.markParsed(pageToParse)) {
                        LOGGER.error("Failed to make page as parsed {}", pageToParse);
                        result = null;
                    }
                } else {
                    LOGGER.error("failed to parse {}", pageToParse);
                }

            } catch (Exception e) {
                LOGGER.error("Failed to parse product page {}", pageToParse.getUrl(), e);
            }
            return result;
        }).filter(webPageEntities -> null != webPageEntities)
                .subscribe(productService::save, ex -> LOGGER.error("Parser Process Exception", ex));
    }
}
