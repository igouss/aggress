package com.naxsoft.parsers.webPageParsers.canadaAmmo;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class CanadaAmmoFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadaAmmoFrontPageParser.class);

    public CanadaAmmoFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        Observable<String> observable = Observable.create(subscriber -> client.get(webPage.getUrl(), new VoidAbstractCompletionHandler(webPage, subscriber)));
        Observable<Set<WebPageEntity>> setObservable = observable.flatMap(url -> Observable.from(client.get(url, new SetAbstractCompletionHandler(url, webPage))));
        return setObservable.flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.canadaammo.com/") && webPage.getType().equals("frontPage");
    }

    private static class VoidAbstractCompletionHandler extends AbstractCompletionHandler<Void> {
        private final WebPageEntity webPage;
        private final Subscriber<? super String> subscriber;

        public VoidAbstractCompletionHandler(WebPageEntity webPage, Subscriber<? super String> subscriber) {
            this.webPage = webPage;
            this.subscriber = subscriber;
        }

        @Override
        public Void onCompleted(Response response) throws Exception {
            if (200 == response.getStatusCode()) {
                Document document = Jsoup.parse(response.getResponseBody(), webPage.getUrl());
                parseDocument(document);
            }
            subscriber.onCompleted();
            return null;
        }

        private void parseDocument(Document document) {
            Elements elements = document.select("ul#menu-main-menu:not(.off-canvas-list) > li > a");
            LOGGER.info("Parsing for sub-pages + {}", webPage.getUrl());

            for (Element el : elements) {
                String url = el.attr("abs:href") + "?count=72";
                subscriber.onNext(url);
            }
        }
    }

    private static class SetAbstractCompletionHandler extends AbstractCompletionHandler<Set<WebPageEntity>> {
        private final String url;
        private final WebPageEntity webPage;

        public SetAbstractCompletionHandler(String url, WebPageEntity webPage) {
            this.url = url;
            this.webPage = webPage;
        }

        @Override
        public Set<WebPageEntity> onCompleted(Response response) throws Exception {

            Document document = Jsoup.parse(response.getResponseBody(), url);
            HashSet<WebPageEntity> subResult = parseDocument(document);
            return subResult;
        }

        private HashSet<WebPageEntity> parseDocument(Document document) {
            HashSet<WebPageEntity> subResult = new HashSet<>();
            Elements elements = document.select("div.clearfix span.pagination a.nav-page");
            if (elements.isEmpty()) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(url);
                webPageEntity.setType("productList");
                webPageEntity.setCategory("n/a");
                LOGGER.info("productList={}, parent={}", webPageEntity.getUrl(), webPage.getUrl());
                subResult.add(webPageEntity);
            } else {
                int i = Integer.parseInt(elements.first().text()) - 1;
                int end = Integer.parseInt(elements.last().text());
                for (; i <= end; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(url + "&page=" + i);
                    webPageEntity.setType("productList");
                    webPageEntity.setCategory("n/a");
                    LOGGER.info("productList={}, parent={}", webPageEntity.getUrl(), webPage.getUrl());
                    subResult.add(webPageEntity);
                }
            }
            return subResult;
        }
    }
}
