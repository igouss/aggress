package com.naxsoft.commands;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.naxsoft.ApplicationComponent;
import com.naxsoft.database.Elastic;
import com.naxsoft.database.ProductService;
import com.naxsoft.database.WebPageService;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.parsers.productParser.ProductParser;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Parse raw web pages entries, generate JSON representation and sent it to Elasticsearch
 */
public class ParseCommand implements Command {
    private final static Logger LOGGER = LoggerFactory.getLogger(ParseCommand.class);

    private final static Set<String> VALID_CATEGORIES = new HashSet<>();
    static {
        VALID_CATEGORIES.add("n/a");
        VALID_CATEGORIES.add("firearms");
        VALID_CATEGORIES.add("reloading");
        VALID_CATEGORIES.add("ammo");
        VALID_CATEGORIES.add("misc");
    }

    @Inject
    protected WebPageService webPageService = null;
    @Inject
    protected ProductService productService = null;
    @Inject
    protected Elastic elastic = null;
    @Inject
    protected ProductParserFactory productParserFactory = null;
    @Inject
    protected MetricRegistry metrics = null;
    @Inject
    protected String indexSuffix = null;
    @Inject
    protected WebPageParserFactory webPageParserFactory;


    @Override
    public void setUp(ApplicationComponent applicationComponent) throws CLIException {
        webPageService = applicationComponent.getWebPageService();
        productService = applicationComponent.getProductService();
        elastic = applicationComponent.getElastic();

    }

    @Override
    public void run() throws CLIException {
        Observable<ProductEntity> products = getProductEntityObservable().onErrorResumeNext(getProductEntityObservable());
        indexProducts(products, "product" + indexSuffix, "guns");
        LOGGER.info("Parsing complete");
    }

    private Observable<ProductEntity> getProductEntityObservable() {
        return webPageService.getUnparsedByType("productPage").flatMap(webPageEntity -> {
            WebPageParser parser = webPageParserFactory.getParser(webPageEntity);
            webPageService.markParsed(webPageEntity);
            return parser.parse(webPageEntity);
        }).map(pageToParse -> {
            Set<ProductEntity> result = null;
            try {
                if (VALID_CATEGORIES.contains(pageToParse.getCategory())) {
                    LOGGER.warn("Invalid category: {}", pageToParse);
                }

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
