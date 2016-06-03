package com.naxsoft.parsers.webPageParsers.tradeexcanada;

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
class TradeexCanadaProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeexCanadaProductPageParser.class);
    private final HttpClient client;

    public TradeexCanadaProductPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        if (parent.getUrl().contains("out-stock") || parent.getUrl().contains("-sold")) {
            return Observable.empty();
        } else {
            return Observable.from(PageDownloader.download(client, parent), Schedulers.io())
                    .filter(data -> {
                        if (null != data) {
                            return true;
                        } else {
                            LOGGER.error("failed to download web page {}", parent.getUrl());
                            return false;
                        }
                    });
        }
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.tradeexcanada.com/") && webPage.getType().equals("productPage");
    }
}