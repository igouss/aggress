package com.naxsoft.modules;

import com.naxsoft.commands.*;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.parsingService.WebPageService;
import com.naxsoft.storage.Persistent;
import com.naxsoft.storage.elasticsearch.Elastic;
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
    static CreateESIndexCommand provideCreateESIndexCommand(Elastic elastic) {
        return new CreateESIndexCommand(elastic);
    }

    @Provides
    @Singleton
    @NotNull
    static ParseCommand provideParseCommand(WebPageService webPageService, ProductParserFactory productParserFactory, Elastic elastic) {
        return new ParseCommand(webPageService, productParserFactory, elastic);
    }

    @Provides
    @Singleton
    @NotNull
    static PopulateDBCommand providePopulateDBCommand(WebPageService webPageService) {
        return new PopulateDBCommand(webPageService);
    }
}