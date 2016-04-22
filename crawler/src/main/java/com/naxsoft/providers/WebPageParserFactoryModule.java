package com.naxsoft.providers;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

@Module(includes = {HttpClientModule.class})
public class WebPageParserFactoryModule {
    @Provides
    @Singleton
    @NotNull
    public static WebPageParserFactory get(HttpClient client) {
        return new WebPageParserFactory(client);
    }
}