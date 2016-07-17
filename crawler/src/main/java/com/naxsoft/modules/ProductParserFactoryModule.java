package com.naxsoft.modules;

import com.naxsoft.parsers.productParser.ProductParserFacade;
import dagger.Module;
import dagger.Provides;

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
    static ProductParserFacade provideProductParserFacade() {
        return new ProductParserFacade();
    }
}