package com.naxsoft;

import com.codahale.metrics.MetricRegistry;
import com.lambdaworks.redis.event.EventBus;
import com.naxsoft.commands.*;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.database.Elastic;
import com.naxsoft.database.Persistent;
import com.naxsoft.database.ProductService;
import com.naxsoft.database.WebPageService;
import com.naxsoft.modules.*;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.scheduler.Scheduler;
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
        , EventBusModule.class
        , CommandModule.class
        , SchedulerModule.class
        , VertxModule.class
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

    EventBus getEventBus();

    Scheduler getScheduler();

    CleanDBCommand getCleanDbCommand();

    CrawlCommand getCrawlCommand();

    CreateESIndexCommand getCreateESIndexCommand();

    CreateESMappingCommand getCreateESMappingCommand();

    ParseCommand getParseCommand();

    PopulateDBCommand getPopulateDBCommand();

    Vertx getVertx();
}
