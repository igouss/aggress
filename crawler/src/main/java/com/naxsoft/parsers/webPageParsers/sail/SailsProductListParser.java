package com.naxsoft.parsers.webPageParsers.sail;

import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
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

/**
 * Copyright NAXSoft 2015
 */
public class SailsProductListParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger logger = LoggerFactory.getLogger(SailsProductListParser.class);
    private static final Collection<Cookie> cookies;

    static {
        cookies = new ArrayList<>(1);
        cookies.add(Cookie.newValidCookie("store_language", "english", false, null, null, Long.MAX_VALUE, false, false));
    }

    public SailsProductListParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.create(subscriber -> client.get(parent.getUrl(), cookies, new CompletionHandler<Void>() {
            @Override
            public Void onCompleted(com.ning.http.client.Response resp) throws Exception {
                if (200 == resp.getStatusCode()) {
                    Document document = Jsoup.parse(resp.getResponseBody(), parent.getUrl());
                    // on first pass we don't specify p=1
                    // add all subpages
                    if (!parent.getUrl().contains("p=")) {
                        Elements elements = document.select(".toolbar-bottom .pages a");
                        for (Element element : elements) {
                            WebPageEntity webPageEntity = new WebPageEntity();
                            webPageEntity.setUrl(element.attr("abs:href"));
                            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                            webPageEntity.setParsed(false);
                            webPageEntity.setType("productList");
                            webPageEntity.setCategory(parent.getCategory());
                            logger.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), parent.getUrl());
                            subscriber.onNext(webPageEntity);
                        }
                    }
                    Elements elements = document.select(".item > a");
                    for (Element element : elements) {
                        WebPageEntity webPageEntity = new WebPageEntity();
                        webPageEntity.setUrl(element.attr("abs:href"));
                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                        webPageEntity.setParsed(false);
                        webPageEntity.setType("productPage");
                        webPageEntity.setCategory(parent.getCategory());
                        logger.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), parent.getUrl());
                        subscriber.onNext(webPageEntity);
                    }
                }
                subscriber.onCompleted();
                return null;
            }
        }));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.sail.ca/") && webPage.getType().equals("productList");
    }
}