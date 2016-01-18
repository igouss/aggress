package com.naxsoft.parsers.webPageParsers.marstar;

import com.naxsoft.crawler.CompletionHandler;
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

import java.sql.Timestamp;

/**
 * Copyright NAXSoft 2015
 */
public class MarstarProductListParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(MarstarProductListParser.class);

    public MarstarProductListParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.create(subscriber -> client.get(parent.getUrl(), new VoidCompletionHandler(parent, subscriber)));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.marstar.ca/") && webPage.getType().equals("productList");
    }

    private static class VoidCompletionHandler extends CompletionHandler<Void> {
        private final WebPageEntity parent;
        private final Subscriber<? super WebPageEntity> subscriber;

        public VoidCompletionHandler(WebPageEntity parent, Subscriber<? super WebPageEntity> subscriber) {
            this.parent = parent;
            this.subscriber = subscriber;
        }

        @Override
        public Void onCompleted(Response response) throws Exception {
            if (200 == response.getStatusCode()) {
                Document document = Jsoup.parse(response.getResponseBody(), parent.getUrl());
                parseDocument(document);
            } else {
                LOGGER.warn("Failed to open page {} error code: {}", response.getUri(), response.getStatusCode());
            }
            subscriber.onCompleted();
            return null;
        }

        private void parseDocument(Document document) {
            LOGGER.info("Parsing {}", document.select("h1").text());
            Elements elements = document.select("#main-content > div > table > tbody > tr > td > a:nth-child(3)");
            for (Element e : elements) {
                WebPageEntity webPageEntity = getProductPage(e);
                subscriber.onNext(webPageEntity);
            }
            elements = document.select(".baseTable td:nth-child(1) > a");
            for (Element e : elements) {
                WebPageEntity webPageEntity = getProductPage(e);
                subscriber.onNext(webPageEntity);
            }
            elements = document.select("div.subcategoryName a");
            for (Element e : elements) {
                WebPageEntity webPageEntity = getProductList(e);
                webPageEntity.setCategory(parent.getCategory());
                subscriber.onNext(webPageEntity);
            }
//                    if (result.isEmpty()) {
//                        logger.warn("No entries found url = {}", resp.getUri());
//                    }
        }

        private static WebPageEntity getProductList(Element e) {
            String linkUrl = e.attr("abs:href") + "&displayOutOfStock=no";
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(linkUrl);
            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
            webPageEntity.setParsed(false);
            webPageEntity.setType("productList");
            LOGGER.info("Found product list page {} url={}", e.text(), linkUrl);
            return webPageEntity;
        }

        private static WebPageEntity getProductPage(Element e) {
            String linkUrl = e.attr("abs:href");
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(linkUrl);
            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
            webPageEntity.setParsed(false);
            webPageEntity.setType("productPage");
            LOGGER.info("Found product {} url={}", e.text(), linkUrl);
            return webPageEntity;
        }
    }
}
