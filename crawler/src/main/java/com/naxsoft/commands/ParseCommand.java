package com.naxsoft.commands;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.ApplicationComponent;
import com.naxsoft.database.Elastic;
import com.naxsoft.database.ProductService;
import com.naxsoft.database.WebPageService;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.productParser.ProductParserFacade;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscription;
import rx.schedulers.Schedulers;

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
        VALID_CATEGORIES.add("firearm");
        VALID_CATEGORIES.add("reload");
        VALID_CATEGORIES.add("optic");
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
    protected ProductParserFacade productParserFactory = null;
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
        webPageParserFactory = applicationComponent.getWebPageParserFactory();
        productParserFactory = applicationComponent.getProductParserFactory();
        elastic = applicationComponent.getElastic();

    }

    @Override
    public void start() throws CLIException {
        indexProducts(getProductEntityObservable(), "product" , indexSuffix, "guns");
        LOGGER.info("Parsing complete");
    }

    /**
     * @return
     */
    private Observable<ProductEntity> getProductEntityObservable() {
        return webPageService.getUnparsedByType("productPage").flatMap(webPageEntity -> {
            Observable<WebPageEntity> result = webPageParserFactory.parse(webPageEntity);
            webPageService.markParsed(webPageEntity).subscribe(
                    res -> LOGGER.info("Marked as parsed {}", res),
                    err -> LOGGER.error("Failed to mark as parsed"),
                    () -> LOGGER.debug("Mark as parse complete")
            );
            LOGGER.info("returning parsed productPages");
            return result;
        }).observeOn(Schedulers.computation())
                .filter(pageToParse -> pageToParse != null)
                .doOnNext(pageToParse -> {
                    String allCategories = pageToParse.getCategory();
                    if (allCategories != null) {
                        for (String category : allCategories.split(",")) {
                            if (category == null || !VALID_CATEGORIES.contains(category.toLowerCase())) {
                                LOGGER.warn("Invalid category: {}", pageToParse);
                            }
                        }
                    }
                }).map(pageToParse -> {
                    Set<ProductEntity> result = new HashSet<>();
                    try {
                        result.addAll(productParserFactory.parse(pageToParse));
                    } catch (Exception e) {
                        LOGGER.error("Failed to parse product page {}", pageToParse.getUrl(), e);
                    }
                    if (result.isEmpty()) {
                        LOGGER.warn("No result on page {}", pageToParse);
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
    private Subscription indexProducts(Observable<ProductEntity> products, String index, String indexSuffix, String type) {
        LOGGER.info("Indexing products");
        Subscription subscription = elastic.index(products, index, indexSuffix, type);
        LOGGER.info("Indexing products compete");
        return subscription;

    }
}
