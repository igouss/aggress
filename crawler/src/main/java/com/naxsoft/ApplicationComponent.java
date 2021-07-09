package com.naxsoft;

import com.naxsoft.commands.*;
import com.naxsoft.http.HttpClient;
import com.naxsoft.modules.*;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.storage.Persistent;
import com.naxsoft.storage.elasticsearch.Elastic;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
        RedisModule.class
        , HttpClientModule.class
        , ElasticModule.class
        , WebPageServiceModule.class
        , ProductServiceModule.class
        , WebPageParserFactoryModule.class
        , ProductParserFactoryModule.class
        , EncoderModule.class
        , EventBusModule.class
        , CommandModule.class
}, dependencies = {})
public interface ApplicationComponent {
    Persistent getDatabase();

    HttpClient getHttpClient();

    Elastic getElastic();

    WebPageParserFactory getWebPageParserFactory();

    ProductParserFactory getProductParserFactory();

    CleanDBCommand getCleanDbCommand();

    CrawlCommand getCrawlCommand();

    CreateESIndexCommand getCreateESIndexCommand();

    ParseCommand getParseCommand();

    PopulateDBCommand getPopulateDBCommand();
}
