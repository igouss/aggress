package com.naxsoft.parsers.webPageParsers.sail;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import io.vertx.core.eventbus.Message;
import org.asynchttpclient.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright NAXSoft 2015
 */
class SailsProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SailsProductPageParser.class);
    private static final List<Cookie> cookies;

    static {
        cookies = new ArrayList<>(1);
        cookies.add(Cookie.newValidCookie("store_language", "english", false, null, null, Long.MAX_VALUE, false, false));
    }

    private final HttpClient client;

    public SailsProductPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.from(PageDownloader.download(client, cookies, webPage), Schedulers.io())
                .filter(data -> null != data);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("sail.ca") && webPage.getType().equals("productPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("sail.ca/productPage", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("webPageParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}