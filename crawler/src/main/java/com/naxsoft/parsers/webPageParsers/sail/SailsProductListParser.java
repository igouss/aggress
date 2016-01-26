package com.naxsoft.parsers.webPageParsers.sail;

import com.naxsoft.crawler.AbstractCompletionHandler;
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
import rx.Subscriber;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Copyright NAXSoft 2015
 */
public class SailsProductListParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(SailsProductListParser.class);
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
        return Observable.create(subscriber -> client.get(parent.getUrl(), cookies, new VoidAbstractCompletionHandler(parent, subscriber)));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.sail.ca/") && webPage.getType().equals("productList");
    }

    private static class VoidAbstractCompletionHandler extends AbstractCompletionHandler<Void> {
        private final WebPageEntity parent;
        private final Subscriber<? super WebPageEntity> subscriber;

        public VoidAbstractCompletionHandler(WebPageEntity parent, Subscriber<? super WebPageEntity> subscriber) {
            this.parent = parent;
            this.subscriber = subscriber;
        }

        @Override
        public Void onCompleted(com.ning.http.client.Response response) throws Exception {
            if (200 == response.getStatusCode()) {
                Document document = Jsoup.parse(response.getResponseBody(), parent.getUrl());
                parseDocument(document);

            }
            subscriber.onCompleted();
            return null;
        }

        private void parseDocument(Document document) {
            // on first pass we don't specify p=1
            // add all subpages
            if (!parent.getUrl().contains("p=")) {
                Elements elements = document.select(".toolbar-bottom .pages a");
                for (Element element : elements) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(element.attr("abs:href"));
                    webPageEntity.setParsed(false);
                    webPageEntity.setType("productList");
                    webPageEntity.setCategory(parent.getCategory());
                    LOGGER.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), parent.getUrl());
                    subscriber.onNext(webPageEntity);
                }
            }
            Elements elements = document.select(".item > a");
            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(element.attr("abs:href"));
                webPageEntity.setParsed(false);
                webPageEntity.setType("productPage");
                webPageEntity.setCategory(parent.getCategory());
                LOGGER.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), parent.getUrl());
                subscriber.onNext(webPageEntity);
            }
        }
    }
}