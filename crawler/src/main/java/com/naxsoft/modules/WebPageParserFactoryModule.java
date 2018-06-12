package com.naxsoft.modules;

import com.naxsoft.http.HttpClient;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

@Module()
public class WebPageParserFactoryModule {
    @Provides
    @Singleton
    @NotNull
    static WebPageParserFactory provideWebPageParserFactory(HttpClient client) {
        return new WebPageParserFactory(client);
    }
}