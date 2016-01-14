package com.naxsoft.parsers.webPageParsers.bullseyelondon;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

public class BullseyelondonProductPageParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(BullseyelondonProductPageParser.class);
    private final AsyncFetchClient client;

    public BullseyelondonProductPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.from(PageDownloader.download(client, webPage.getUrl()))
                .filter(data -> {
                    if (null != data) {
                        return true;
                    } else {
                        logger.error("failed to download web page {}" + webPage.getUrl());
                        return false;
                    }
                })
                .map(webPageEntity -> {
                    webPageEntity.setCategory(webPage.getCategory());
                    return webPageEntity;
                });
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.bullseyelondon.com/") && webPage.getType().equals("productPage");
    }
}
