package com.naxsoft;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.database.*;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.providers.*;
import dagger.Component;

import javax.inject.Singleton;

/**
 * Copyright NAXSoft 2015
 */
@Singleton
@Component(modules = {
        PersistentModule.class
        , HttpClientModule.class
        , ElasticModule.class
        , MetricsRegistryModule.class
        , WebPageServiceModule.class
        , SourceServiceModule.class
        , ProductServiceModule.class
        , WebPageParserFactoryModule.class
}, dependencies = {})
public interface ApplicationComponent {
    Persistent getDatabase();

    HttpClient getHttpClient();

    WebPageService getWebPageService();

    Elastic getElastic();

    SourceService getSourceService();

    WebPageParserFactory getWebPageParserFactory();

    ProductService getProductService();

    MetricRegistry getMetricRegistry();
}
