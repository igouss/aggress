package com.naxsoft;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.database.*;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import joptsimple.OptionSet;

import java.util.HashMap;

/**
 * Copyright NAXSoft 2015
 */
public class ExecutionContext extends HashMap<String, Object> {
    private OptionSet options;
    private Database db;
    private Elastic elastic;
    private ScheduledReporter elasticReporter;
    private WebPageParserFactory webPageParserFactory;
    private WebPageService webPageService;
    private ProductService productService;
    private SourceService sourceService;
    private ProductParserFactory productParserFactory;
    private MetricRegistry metrics;
    private String indexSuffix;
    private HttpClient fetchClient;

    public HttpClient getHTTPClient() {
        return fetchClient;
    }

    public void setFetchClient(HttpClient fetchClient) {
        this.fetchClient = fetchClient;
    }

    public String getIndexSuffix() {
        return indexSuffix;
    }

    public void setIndexSuffix(String indexSuffix) {
        this.indexSuffix = indexSuffix;
    }

    public OptionSet getOptions() {
        return options;
    }

    public void setOptions(OptionSet options) {
        this.options = options;
    }

    public Database getDb() {
        return db;
    }

    public void setDb(Database db) {
        this.db = db;
    }

    public Elastic getElastic() {
        return elastic;
    }

    public void setElastic(Elastic elastic) {
        this.elastic = elastic;
    }

    public ScheduledReporter getElasticReporter() {
        return elasticReporter;
    }

    public void setElasticReporter(ScheduledReporter elasticReporter) {
        this.elasticReporter = elasticReporter;
    }

    public WebPageParserFactory getWebPageParserFactory() {
        return webPageParserFactory;
    }

    public void setWebPageParserFactory(WebPageParserFactory webPageParserFactory) {
        this.webPageParserFactory = webPageParserFactory;
    }

    public WebPageService getWebPageService() {
        return webPageService;
    }

    public void setWebPageService(WebPageService webPageService) {
        this.webPageService = webPageService;
    }

    public ProductService getProductService() {
        return productService;
    }

    public void setProductService(ProductService productService) {
        this.productService = productService;
    }

    public SourceService getSourceService() {
        return sourceService;
    }

    public void setSourceService(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    public ProductParserFactory getProductParserFactory() {
        return productParserFactory;
    }

    public void setProductParserFactory(ProductParserFactory productParserFactory) {
        this.productParserFactory = productParserFactory;
    }

    public MetricRegistry getMetrics() {
        return metrics;
    }

    public void setMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
    }
}
