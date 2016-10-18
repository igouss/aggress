package com.naxsoft.parsers.webPageParsers.tradeexcanada;

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
class TradeexCanadaProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeexCanadaProductPageParser.class);
    private final HttpClient client;

    private TradeexCanadaProductPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        LOGGER.trace("Processing productPage {}", webPage.getUrl());
        if (webPage.getUrl().contains("out-stock") || webPage.getUrl().contains("-sold")) {
            return Observable.empty();
        } else {
            return PageDownloader.download(client, webPage, "productPageRaw")
                    .filter(data -> {
                        if (null != data) {
                            return true;
                        } else {
                            LOGGER.error("failed to download web page {}", webPage.getUrl());
                            return false;
                        }
                    });
        }
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("tradeexcanada.com") && webPage.getType().equals("productPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("tradeexcanada.com/productPage", getParseRequestMessageHandler());
    }
}