package com.naxsoft.modules;

import com.naxsoft.parsers.productParser.ProductParserFactory;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright NAXSoft 2015
 */
@Module()
public class ProductParserFactoryModule {
    @Provides
    @Singleton
    @NotNull
    static ProductParserFactory provideProductParserFacade(Vertx vertx) {
        return new ProductParserFactory(vertx);
    }
}