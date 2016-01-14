package com.naxsoft.parsers.webPageParsers.leverarms;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;

/**
 * Copyright NAXSoft 2015
 */
public class LeverarmsProductParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(LeverarmsProductParser.class);
    private final AsyncFetchClient client;

    public LeverarmsProductParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return PageDownloader.download(client, webPage.getUrl());
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.leverarms.com/") && webPage.getType().equals("productPage");
    }
}