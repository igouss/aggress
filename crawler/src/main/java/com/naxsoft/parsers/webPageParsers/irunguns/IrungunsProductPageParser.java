package com.naxsoft.parsers.webPageParsers.irunguns;

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
public class IrungunsProductPageParser extends AbstractWebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(IrungunsProductPageParser.class);

    public IrungunsProductPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return PageDownloader.download(client, webPage.getUrl());
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.irunguns.us/") && webPage.getType().equals("productPage");
    }
}
