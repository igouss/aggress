package com.naxsoft.modules;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright NAXSoft 2015
 */
@Module(includes = {VertxModule.class})
public class WebPageParserFactoryModule {
    @Provides
    @Singleton
    @NotNull
    static WebPageParserFactory provideWebPageParserFactory(Vertx vertx, HttpClient client) {
        return new WebPageParserFactory(vertx, client);
    }
}