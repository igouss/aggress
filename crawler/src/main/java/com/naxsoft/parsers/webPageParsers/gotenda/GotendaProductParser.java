package com.naxsoft.parsers.webPageParsers.gotenda;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Copyright NAXSoft 2015
 */
class GotendaProductParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GotendaProductParser.class);
    private final HttpClient client;

    public GotendaProductParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.from(PageDownloader.download(client, webPage), Schedulers.io())
                .filter(data -> null != data);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("gotenda.com") && webPage.getType().equals("productPage");
    }
}