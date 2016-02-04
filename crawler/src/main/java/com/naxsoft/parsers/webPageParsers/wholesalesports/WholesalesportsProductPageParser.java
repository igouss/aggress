package com.naxsoft.parsers.webPageParsers.wholesalesports;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * Copyright NAXSoft 2015
 */
public class WholesalesportsProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WholesalesportsProductPageParser.class);
    private final HttpClient client;

    public WholesalesportsProductPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.from(PageDownloader.download(client, webPage))
                .filter(data -> {
                    if (null != data) {
                        return true;
                    } else {
                        LOGGER.error("failed to download web page {}", webPage.getUrl());
                        return false;
                    }
                }).map(webPageEntity -> {
                    webPageEntity.setCategory(webPage.getCategory());
                    return webPageEntity;
                });
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.wholesalesports.com/") && webPage.getType().equals("productPage");
    }
}
