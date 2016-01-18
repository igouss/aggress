package com.naxsoft.parsers.webPageParsers.theammosource;

import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.sql.Timestamp;

/**
 * Copyright NAXSoft 2015
 */
public class TheammosourceProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TheammosourceProductListParser.class);
    private final HttpClient client;

    public TheammosourceProductListParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.create(subscriber -> client.get(parent.getUrl(), new VoidCompletionHandler(parent, subscriber)));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.theammosource.com/") && webPage.getType().equals("productList");
    }

    private static class VoidCompletionHandler extends CompletionHandler<Void> {
        private final WebPageEntity parent;
        private final Subscriber<? super WebPageEntity> subscriber;

        public VoidCompletionHandler(WebPageEntity parent, Subscriber<? super WebPageEntity> subscriber) {
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
            Elements elements = document.select(".categoryListBoxContents > a");
            if (!elements.isEmpty()) {
                for (Element element : elements) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(element.attr("abs:href"));
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setType("productList");
                    webPageEntity.setCategory(parent.getCategory());
                    LOGGER.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), parent.getUrl());
                    subscriber.onNext(webPageEntity);
                }
            } else {
                if (!parent.getUrl().contains("&page=")) {
                    elements = document.select("#productsListingListingTopLinks > a");
                    for (Element element : elements) {
                        WebPageEntity webPageEntity = new WebPageEntity();
                        webPageEntity.setUrl(element.attr("abs:href"));
                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                        webPageEntity.setParsed(false);
                        webPageEntity.setType("productList");
                        webPageEntity.setCategory(parent.getCategory());
                        LOGGER.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), parent.getUrl());
                        subscriber.onNext(webPageEntity);
                    }
                }
                elements = document.select(".itemTitle a");
                for (Element element : elements) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(element.attr("abs:href"));
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setType("productPage");
                    LOGGER.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), parent.getUrl());
                    subscriber.onNext(webPageEntity);
                }
            }
        }
    }
}