package com.naxsoft.parsers.webPageParsers.dantesports;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import com.ning.http.client.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright NAXSoft 2015
 */
public class DantesportsProductPageParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(DantesportsProductPageParser.class);
    private final AsyncFetchClient client;

    public DantesportsProductPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    public Observable<WebPageEntity> parse(WebPageEntity webPage)  {
        return PageDownloader.download(client, webPage.getUrl());
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://shop.dantesports.com/") && webPage.getType().equals("productPage");
    }
}
