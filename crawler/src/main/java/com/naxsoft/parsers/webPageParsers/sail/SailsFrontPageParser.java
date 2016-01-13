package com.naxsoft.parsers.webPageParsers.sail;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Copyright NAXSoft 2015
 */
public class SailsFrontPageParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(SailsFrontPageParser.class);
    private static final Collection<Cookie> cookies;

    static {
        cookies = new ArrayList<>(1);
        cookies.add(Cookie.newValidCookie("store_language", "english", false, null, null, Long.MAX_VALUE, false, false));
    }

    private final AsyncFetchClient client;

    public SailsFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://www.sail.ca/en/hunting/firearms"));
//        webPageEntities.add(create("http://www.sail.ca/en/hunting/firearm-accessories", parent));
//        webPageEntities.add(create("http://www.sail.ca/en/hunting/ammunition", parent));
        return Observable.create(subscriber -> {
            Observable.from(webPageEntities).
                    flatMap(page -> Observable.from(client.get(page.getUrl(), cookies, new CompletionHandler<Void>() {
                        @Override
                        public Void onCompleted(Response resp) throws Exception {
                            if (200 == resp.getStatusCode()) {
                                Document document = Jsoup.parse(resp.getResponseBody(), page.getUrl());
                                Elements elements = document.select("ol.nav-2 a");

                                for (Element el : elements) {
                                    WebPageEntity webPageEntity = new WebPageEntity();
                                    webPageEntity.setUrl(el.attr("abs:href") + "?limit=36");
                                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                    webPageEntity.setParsed(false);
                                    webPageEntity.setStatusCode(resp.getStatusCode());
                                    webPageEntity.setType("productList");
                                    logger.info("Product page listing={}", webPageEntity.getUrl());
                                    subscriber.onNext(webPageEntity);
                                }
                            }
                            subscriber.onCompleted();
                            return null;
                        }
                    })));
        });
    }

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
        webPageEntity.setParsed(false);
        webPageEntity.setStatusCode(200);
        webPageEntity.setType("productList");
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.sail.ca/") && webPage.getType().equals("frontPage");
    }
}