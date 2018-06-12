package com.naxsoft.modules;

import com.naxsoft.parsers.productParser.ProductParserFactory;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

@Module()
public class ProductParserFactoryModule {
    @Provides
    @Singleton
    @NotNull
    static ProductParserFactory provideProductParserFacade() {
        return new ProductParserFactory();
    }
}