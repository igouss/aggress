package com.naxsoft.parsers.webPageParsers.bullseyelondon;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;

public class BullseyelondonProductPageParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(BullseyelondonProductPageParser.class);
    private final AsyncFetchClient client;

    public BullseyelondonProductPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return PageDownloader.download(client, webPage.getUrl());

    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.bullseyelondon.com/") && webPage.getType().equals("productPage");
    }
}
