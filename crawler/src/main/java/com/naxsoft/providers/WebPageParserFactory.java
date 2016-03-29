package com.naxsoft.providers;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright NAXSoft 2015
 */
@Module
public class WebPageParserFactory {
    @Provides
    @Singleton
    @NotNull
    public WebPageParserFactory get() {
        return new WebPageParserFactory();
    }
}
