package com.naxsoft.parsers.webPageParsers.dantesports;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.ning.http.client.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

/**
 * Copyright NAXSoft 2015
 */
public class DantesportsProductPageParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(DantesportsProductPageParser.class);
    private final AsyncFetchClient client;

    public DantesportsProductPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    private static CompletionHandler<List<Cookie>> getEngCookiesHandler() {
        return new CompletionHandler<List<Cookie>>() {
            @Override
            public List<Cookie> onCompleted(com.ning.http.client.Response resp) throws Exception {
                return resp.getCookies();
            }
        };
    }

    public Observable<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        return Observable.create(subscriber -> {
            Observable.from(client.get("https://shop.dantesports.com/set_lang.php?lang=EN", new LinkedList<>(), getEngCookiesHandler(), false)).subscribe(cookies -> {
                client.get(webPage.getUrl(), cookies, new CompletionHandler<Void>() {
                    @Override
                    public Void onCompleted(com.ning.http.client.Response resp) throws Exception {
                        if (200 == resp.getStatusCode()) {
                            WebPageEntity webPageEntity = new WebPageEntity();
                            webPageEntity.setUrl(webPage.getUrl());
                            webPageEntity.setContent(compress(resp.getResponseBody()));
                            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                            webPageEntity.setParsed(false);
                            webPageEntity.setStatusCode(resp.getStatusCode());
                            webPageEntity.setType("productPageRaw");
                            logger.info("productPageRaw={}", webPageEntity.getUrl());
                            subscriber.onNext(webPageEntity);
                        }
                        subscriber.onCompleted();
                        return null;
                    }
                });
            });
        });
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://shop.dantesports.com/") && webPage.getType().equals("productPage");
    }
}
