package com.naxsoft.parsers.webPageParsers.crafm;

import com.naxsoft.crawler.AbstractCompletionHandler;
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

/**
 * Copyright NAXSoft 2015
 */
public class CrafmFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(CrafmFrontPageParser.class);

    public CrafmFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.create(subscriber -> client.get("http://www.crafm.com/firearms.html?limit=all", new VoidAbstractCompletionHandler(webPage, subscriber)));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().equals("http://www.crafm.com/") && webPage.getType().equals("frontPage");
    }

    private static class VoidAbstractCompletionHandler extends AbstractCompletionHandler<Void> {
        private final WebPageEntity webPage;
        private final Subscriber<? super WebPageEntity> subscriber;

        public VoidAbstractCompletionHandler(WebPageEntity webPage, Subscriber<? super WebPageEntity> subscriber) {
            this.webPage = webPage;
            this.subscriber = subscriber;
        }

        @Override
        public Void onCompleted(com.ning.http.client.Response response) throws Exception {
            if (200 == response.getStatusCode()) {
                Document document = Jsoup.parse(response.getResponseBody(), webPage.getUrl());
                parseDocument(document);
            }
            subscriber.onCompleted();
            return null;
        }

        private void parseDocument(Document document) {
            Elements elements = document.select(".products-grid .item > a");
            for (Element e : elements) {
                String linkUrl = e.attr("abs:href");
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(linkUrl);
                webPageEntity.setParsed(false);
                webPageEntity.setType("productPage");
                webPageEntity.setCategory("n/a");
                LOGGER.info("ProductPageUrl={}, parseUrl={}", linkUrl, webPage.getUrl());
                subscriber.onNext(webPageEntity);
            }
        }
    }
}
