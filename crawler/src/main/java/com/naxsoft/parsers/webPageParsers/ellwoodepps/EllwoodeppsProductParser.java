package com.naxsoft.parsers.webPageParsers.ellwoodepps;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * Copyright NAXSoft 2015
 */
class EllwoodeppsProductParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(EllwoodeppsProductParser.class);
    private final HttpClient client;

    public EllwoodeppsProductParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return PageDownloader.download(client, webPage, "productPageRaw")
                .filter(data -> null != data);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("ellwoodepps.com") && webPage.getType().equals("productPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("ellwoodepps.com/productPage", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("webPageParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}