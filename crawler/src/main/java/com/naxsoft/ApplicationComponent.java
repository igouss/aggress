package com.naxsoft;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.commands.*;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.modules.*;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.parsingService.ShellService;
import com.naxsoft.storage.Persistent;
import com.naxsoft.storage.elasticsearch.Elastic;
import dagger.Component;
import io.vertx.core.Vertx;

import javax.inject.Singleton;

/**
 * Copyright NAXSoft 2015
 */
@Singleton
@Component(modules = {
        RedisModule.class
        , HttpClientModule.class
        , ElasticModule.class
        , MetricsRegistryModule.class
        , WebPageServiceModule.class
        , ProductServiceModule.class
        , WebPageParserFactoryModule.class
        , ProductParserFactoryModule.class
        , EncoderModule.class
        , CommandModule.class
        , VertxModule.class
        , ShellServiceModule.class
}, dependencies = {})
interface ApplicationComponent {
    Persistent getDatabase();

    HttpClient getHttpClient();

    Elastic getElastic();

    WebPageParserFactory getWebPageParserFactory();

    ProductParserFactory getProductParserFactory();

    MetricRegistry getMetricRegistry();

    CleanDBCommand getCleanDbCommand();

    CrawlCommand getCrawlCommand();

    CreateESIndexCommand getCreateESIndexCommand();

    ParseCommand getParseCommand();

    PopulateDBCommand getPopulateDBCommand();

    Vertx getVertx();

    ShellService getRemoteAccess();
}
