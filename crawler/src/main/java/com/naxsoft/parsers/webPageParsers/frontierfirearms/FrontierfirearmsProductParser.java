package com.naxsoft.parsers.webPageParsers.frontierfirearms;

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
class FrontierfirearmsProductParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrontierfirearmsProductParser.class);
    private final HttpClient client;

    private FrontierfirearmsProductParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        LOGGER.trace("Processing productPage {}", webPage.getUrl());
        return PageDownloader.download(client, webPage, "productPageRaw")
                .filter(data -> null != data);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("frontierfirearms.ca") && webPage.getType().equals("productPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("frontierfirearms.ca/productPage", getParseRequestMessageHandler());
    }
}