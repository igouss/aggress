package com.naxsoft;

import com.naxsoft.commands.*;
import com.naxsoft.crawler.parsers.parsers.webPageParsers.WebPageParserFactory;
import com.naxsoft.http.HttpClient;
import com.naxsoft.storage.Persistent;
import com.naxsoft.storage.elasticsearch.Elastic;

public interface ApplicationComponent {
    Persistent getDatabase();

    HttpClient getHttpClient();

    Elastic getElastic();

    WebPageParserFactory getWebPageParserFactory();

    CleanDBCommand getCleanDbCommand();

    CrawlCommand getCrawlCommand();

    CreateESIndexCommand getCreateESIndexCommand();

    ParseCommand getParseCommand();

    PopulateDBCommand getPopulateDBCommand();
}
