package com.naxsoft.parsers.webPageParsers.hical;

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

/**
 * Copyright NAXSoft 2015
 */
public class HicalFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(HicalFrontPageParser.class);

    public HicalFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        if (parent.getUrl().equals("http://www.hical.ca/")) {
            webPageEntities.add(create("http://www.hical.ca/new-category/"));
            webPageEntities.add(create("http://www.hical.ca/firearm-accessories/"));
            webPageEntities.add(create("http://www.hical.ca/magazines/"));
            webPageEntities.add(create("http://www.hical.ca/stocks/"));
            webPageEntities.add(create("http://www.hical.ca/tools/"));
            webPageEntities.add(create("http://www.hical.ca/sights-optics/"));
            webPageEntities.add(create("http://www.hical.ca/soft-goods/"));
        } else {
            webPageEntities.add(parent);
        }

        return Observable.create(subscriber -> Observable.from(webPageEntities).
                flatMap(page -> Observable.from(client.get(page.getUrl(), new VoidAbstractCompletionHandler(page, subscriber)))));
    }

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.hical.ca/") && webPage.getType().equals("frontPage");
    }

    private static class VoidAbstractCompletionHandler extends AbstractCompletionHandler<Void> {
        private final WebPageEntity page;
        private final Subscriber<? super WebPageEntity> subscriber;

        public VoidAbstractCompletionHandler(WebPageEntity page, Subscriber<? super WebPageEntity> subscriber) {
            this.page = page;
            this.subscriber = subscriber;
        }

        @Override
        public Void onCompleted(Response response) throws Exception {
            if (200 == response.getStatusCode()) {
                Document document = Jsoup.parse(response.getResponseBody(), page.getUrl());
                parseDocument(response, document);
            }
            subscriber.onCompleted();
            return null;
        }

        private void parseDocument(Response response, Document document) {
            Elements elements;

            // Add sub categories
            if (!response.getUri().toString().contains("page=")) {
                elements = document.select(".SubCategoryList a");
                for (Element el : elements) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(el.attr("abs:href"));
                    webPageEntity.setParsed(false);
                    webPageEntity.setType("frontPage");
                    webPageEntity.setCategory("n/a");
                    LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                    subscriber.onNext(webPageEntity);
                }
                // add subpages
                elements = document.select("#CategoryPagingTop > div > ul > li > a");
                for (Element el : elements) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(el.attr("abs:href"));
                    webPageEntity.setParsed(false);
                    webPageEntity.setType("productList");
                    webPageEntity.setCategory("n/a");
                    LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                    subscriber.onNext(webPageEntity);
                }
            }
            // add current page
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(response.getUri() + "?sort=featured&page=1");
            webPageEntity.setParsed(false);
            webPageEntity.setType("productList");
            webPageEntity.setCategory("n/a");
            LOGGER.info("Product page listing={}", webPageEntity.getUrl());
            subscriber.onNext(webPageEntity);
        }
    }
}