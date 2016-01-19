package com.naxsoft.parsers.webPageParsers.westrifle;

import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.sql.Timestamp;
import java.util.HashSet;

/**
 * Copyright NAXSoft 2015
 */
public class WestrifleFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(WestrifleFrontPageParser.class);

    public WestrifleFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://westrifle.com/wrstore/index.php?main_page=products_all&disp_order=1"));
        return Observable.create(subscriber -> Observable.from(webPageEntities).
                flatMap(page -> Observable.from(client.get(page.getUrl(), new VoidCompletionHandler(page, subscriber)))));
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
        return webPage.getUrl().startsWith("http://westrifle.com/") && webPage.getType().equals("frontPage");
    }

    private static class VoidCompletionHandler extends CompletionHandler<Void> {
        private final WebPageEntity page;
        private final Subscriber<? super WebPageEntity> subscriber;

        public VoidCompletionHandler(WebPageEntity page, Subscriber<? super WebPageEntity> subscriber) {
            this.page = page;
            this.subscriber = subscriber;
        }

        @Override
        public Void onCompleted(Response response) throws Exception {
            if (200 == response.getStatusCode()) {
                Document document = Jsoup.parse(response.getResponseBody(), page.getUrl());
                parseDocument(document);
            }
            subscriber.onCompleted();
            return null;
        }

        private void parseDocument(Document document) {
            Elements elements = document.select("#allProductsListingTopNumber > strong:nth-child(3)");
            int productTotal = Integer.parseInt(elements.text());
            int pageTotal = (int) Math.ceil(productTotal / 10.0);

            for (int i = 1; i <= pageTotal; i++) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(page.getUrl() + "&page=" + i);
                webPageEntity.setParsed(false);
                webPageEntity.setType("productList");
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                subscriber.onNext(webPageEntity);
            }
        }
    }
}