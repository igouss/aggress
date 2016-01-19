package com.naxsoft.parsers.webPageParsers.gunshop;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class GunshopFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(GunshopFrontPageParser.class);

    public GunshopFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(String url, WebPageEntity parent) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        return webPageEntity;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://gun-shop.ca/shop/", parent));

        return Observable.create(subscriber -> Observable.from(webPageEntities).
                flatMap(page -> Observable.from(client.get(page.getUrl(), new VoidCompletionHandler(parent, page, subscriber)))));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://gun-shop.ca/") && webPage.getType().equals("frontPage");
    }

    private static class VoidCompletionHandler extends CompletionHandler<Void> {
        private final WebPageEntity parent;
        private final WebPageEntity page;
        private final Subscriber<? super WebPageEntity> subscriber;

        public VoidCompletionHandler(WebPageEntity parent, WebPageEntity page, Subscriber<? super WebPageEntity> subscriber) {
            this.parent = parent;
            this.page = page;
            this.subscriber = subscriber;
        }

        @Override
        public Void onCompleted(Response response) throws Exception {
            if (200 == response.getStatusCode()) {
                Document document = Jsoup.parse(response.getResponseBody(), parent.getUrl());
                parseDocument(document);
            }
            subscriber.onCompleted();
            return null;
        }

        private void parseDocument(Document document) {
            Elements elements = document.select(".woocommerce-result-count");
            Matcher matcher = Pattern.compile("(\\d+) of (\\d+)").matcher(elements.text());
            if (matcher.find()) {
                int max = Integer.parseInt(matcher.group(2));
                int postsPerPage = Integer.parseInt(matcher.group(1));
                int pages = (int) Math.ceil((double) max / postsPerPage);

                for (int i = 1; i <= pages; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(page.getUrl() + "/page/" + i + "/");
                    webPageEntity.setParsed(false);
                    webPageEntity.setType("productList");
                    LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                    subscriber.onNext(webPageEntity);
                }
            } else {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(page.getUrl());
                webPageEntity.setParsed(false);
                webPageEntity.setType("productList");
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                subscriber.onNext(webPageEntity);
            }
        }
    }
}