package com.naxsoft.parsers.webPageParsers.crafm;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Copyright NAXSoft 2015
 */
public class CrafmProductPageParser extends AbstractWebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(CrafmProductPageParser.class);
    Collection<Cookie> cookies;

    public CrafmProductPageParser(AsyncFetchClient client) {
        this.client = client;
        cookies = new ArrayList<>(1);
        cookies.add(Cookie.newValidCookie("store", "english", false, null, null, Long.MAX_VALUE, false, false));
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.create(subscriber -> {
            client.get(webPage.getUrl(), cookies, new CompletionHandler<Void>() {
                @Override
                public Void onCompleted(Response resp) throws Exception {
                    if (200 == resp.getStatusCode()) {
                        WebPageEntity webPageEntity = new WebPageEntity();
                        webPageEntity.setUrl(webPage.getUrl());
                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                        webPageEntity.setType("productPageRaw");
                        webPageEntity.setContent(compress(resp.getResponseBody()));
                        subscriber.onNext(webPageEntity);
                        logger.info("productPageRaw={}", webPageEntity.getUrl());
                    }
                    subscriber.onCompleted();
                    return null;
                }
            });
        });
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.crafm.com/") && webPage.getType().equals("productPage");
    }
}
