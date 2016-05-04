package com.naxsoft;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.database.Elastic;
import com.naxsoft.database.Persistent;
import com.naxsoft.database.ProductService;
import com.naxsoft.database.WebPageService;
import com.naxsoft.parsers.productParser.ProductParserFactory;
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
        , ProductServiceModule.class
        , WebPageParserFactoryModule.class
        , ProductParserFactoryModule.class
        , EncoderModule.class
}, dependencies = {})
public interface ApplicationComponent {
    Persistent getDatabase();

    HttpClient getHttpClient();

    WebPageService getWebPageService();

    Elastic getElastic();

    WebPageParserFactory getWebPageParserFactory();

    ProductParserFactory getProductParserFactory();

    ProductService getProductService();

    MetricRegistry getMetricRegistry();
}
