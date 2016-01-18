package com.naxsoft.parsers.webPageParsers.sail;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import com.ning.http.client.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright NAXSoft 2015
 */
public class SailsProductPageParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(SailsProductPageParser.class);
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
        return Observable.from(PageDownloader.download(client, cookies, webPage.getUrl()))
                .filter(data -> {
                    if (null != data) {
                        return true;
                    } else {
                        logger.error("failed to download web page {}", webPage.getUrl());
                        return false;
                    }
                }).map(webPageEntity -> {
                    webPageEntity.setCategory(webPage.getCategory());
                    return webPageEntity;
                });
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.sail.ca/") && webPage.getType().equals("productPage");
    }
}