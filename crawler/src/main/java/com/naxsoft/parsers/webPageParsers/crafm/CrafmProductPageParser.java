package com.naxsoft.parsers.webPageParsers.crafm;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.asynchttpclient.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright NAXSoft 2015
 */
class CrafmProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrafmProductPageParser.class);
    private final HttpClient client;
    private final List<Cookie> cookies;

    private CrafmProductPageParser(HttpClient client) {
        this.client = client;
        cookies = new ArrayList<>(1);
        cookies.add(Cookie.newValidCookie("store", "english", false, null, null, Long.MAX_VALUE, false, false));
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        LOGGER.trace("Processing productPage {}", webPage.getUrl());
        return PageDownloader.download(client, cookies, webPage, "productPageRaw")
                .filter(data -> null != data);
    }

    @Override
    public String getParserType() {
        return "productPage";
    }

    @Override
    public String getSite() {
        return "crafm.com";
    }


}
