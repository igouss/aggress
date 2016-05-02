package com.naxsoft.providers;

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
    public static ProductParserFactory get() {
        return new ProductParserFactory();
    }
}