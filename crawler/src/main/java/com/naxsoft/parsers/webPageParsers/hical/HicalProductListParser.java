package com.naxsoft.parsers.webPageParsers.hical;

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
public class HicalProductListParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(HicalProductListParser.class);

    public HicalProductListParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.create(subscriber -> client.get(parent.getUrl(), new VoidCompletionHandler(parent, subscriber)));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.hical.ca/") && webPage.getType().equals("productList");
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
            Elements elements = document.select("#frmCompare .ProductDetails a");
            for (Element el : elements) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(el.attr("abs:href"));
                webPageEntity.setParsed(false);
                webPageEntity.setType("productPage");
                webPageEntity.setCategory(parent.getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                subscriber.onNext(webPageEntity);
            }
        }
    }
}