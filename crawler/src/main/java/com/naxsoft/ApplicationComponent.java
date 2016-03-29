package com.naxsoft;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.database.*;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.providers.DatabaseModule;
import com.naxsoft.providers.ElasticModule;
import com.naxsoft.providers.HttpClientModule;
import com.naxsoft.providers.MetricsRegistryModule;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Copyright NAXSoft 2015
 */
@Singleton
@Component(modules = {
        DatabaseModule.class
        , HttpClientModule.class
        , ElasticModule.class
        , MetricsRegistryModule.class
}, dependencies = {})
public interface ApplicationComponent {
    Database getDatabase();

    HttpClient getHttpClient();

    WebPageService getWebPageService();

    Elastic getElastic();

    SourceService getSourceService();

    WebPageParserFactory getWebPageParserFactory();

    ProductService getProductService();

    MetricRegistry getMetricRegistry();
}
