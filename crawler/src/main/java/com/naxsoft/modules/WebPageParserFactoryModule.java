package com.naxsoft.modules;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

@Module()
public class WebPageParserFactoryModule {
    @Provides
    @Singleton
    @NotNull
    static WebPageParserFactory provideWebPageParserFactory(Vertx vertx, HttpClient client, MetricRegistry metricRegistry) {
        return new WebPageParserFactory(vertx, client, metricRegistry);
    }
}