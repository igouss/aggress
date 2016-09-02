package com.naxsoft.modules;

import com.naxsoft.commands.*;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.database.Elastic;
import com.naxsoft.database.Persistent;
import com.naxsoft.database.WebPageService;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;


/**
 * Copyright NAXSoft 2015
 */
@Module
public class CommandModule {
    @Provides
    @Singleton
    @NotNull
    static CleanDBCommand provideCleanDbCommand(Persistent db) {
        return new CleanDBCommand(db);
    }

    @Provides
    @Singleton
    @NotNull
    static CrawlCommand provideCrawlCommand(WebPageService webPageService, WebPageParserFactory webPageParserFactory) {
        return new CrawlCommand(webPageService, webPageParserFactory);
    }


    @Provides
    @Singleton
    @NotNull
    static CreateESIndexCommand provideCreateESIndexCommand(Elastic elastic, HttpClient httpClient) {
        return new CreateESIndexCommand(elastic, httpClient);
    }

    @Provides
    @Singleton
    @NotNull
    static CreateESMappingCommand provideCreateESMappingCommand(Elastic elastic, HttpClient httpClient) {
        return new CreateESMappingCommand(elastic, httpClient);
    }

    @Provides
    @Singleton
    @NotNull
    static ParseCommand provideParseCommand(WebPageService webPageService, ProductParserFactory productParserFactory, WebPageParserFactory webPageParserFactory, Elastic elastic) {
        return new ParseCommand(webPageService, productParserFactory, webPageParserFactory, elastic);
    }

    @Provides
    @Singleton
    @NotNull
    static PopulateDBCommand providePopulateDBCommand(WebPageService webPageService) {
        return new PopulateDBCommand(webPageService);
    }
}