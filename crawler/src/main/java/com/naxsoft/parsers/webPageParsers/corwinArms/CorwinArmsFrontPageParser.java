package com.naxsoft.parsers.webPageParsers.corwinArms;

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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class CorwinArmsFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(CorwinArmsFrontPageParser.class);

    public CorwinArmsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {

        Future<Set<WebPageEntity>> future = client.get(webPage.getUrl(), new SetCompletionHandler(webPage));

        return Observable.from(future).
                flatMap(Observable::from).
                flatMap(parent -> Observable.create(subscriber -> client.get(parent.getUrl(), new SetCompletionHandler2(parent, subscriber))));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().equals("https://www.corwin-arms.com/") && webPage.getType().equals("frontPage");
    }

    private static class SetCompletionHandler extends CompletionHandler<Set<WebPageEntity>> {
        private final WebPageEntity webPage;

        public SetCompletionHandler(WebPageEntity webPage) {
            this.webPage = webPage;
        }

        @Override
        public Set<WebPageEntity> onCompleted(Response response) throws Exception {
            if (200 == response.getStatusCode()) {
                Document document = Jsoup.parse(response.getResponseBody(), webPage.getUrl());
                return parseDocument(document);
            }
            return Collections.EMPTY_SET;
        }

        private HashSet<WebPageEntity> parseDocument(Document document) {
            HashSet<WebPageEntity> result = new HashSet<>();
            Elements elements = document.select("#block-menu-menu-catalogue > div > ul a");
            for (Element e : elements) {
                String linkUrl = e.attr("abs:href");

                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(linkUrl);
                webPageEntity.setParsed(false);
                webPageEntity.setType("productList");
                LOGGER.info("Found on front page ={}", linkUrl);
                result.add(webPageEntity);
            }
            return result;
        }
    }

    private static class SetCompletionHandler2 extends CompletionHandler<Set<WebPageEntity>> {
        private final WebPageEntity parent;
        private final Subscriber<? super WebPageEntity> subscriber;

        public SetCompletionHandler2(WebPageEntity parent, Subscriber<? super WebPageEntity> subscriber) {
            this.parent = parent;
            this.subscriber = subscriber;
        }

        @Override
        public Set<WebPageEntity> onCompleted(Response response) throws Exception {

            if (200 == response.getStatusCode()) {
                Document document = Jsoup.parse(response.getResponseBody(), parent.getUrl());
                parseDocument(document);
            }
            subscriber.onCompleted();
            return null;
        }

        private void parseDocument(Document document) {
            Elements elements = document.select(".pager li.pager-current");
            Matcher matcher = Pattern.compile("(\\d+) of (\\d+)").matcher(elements.text());
            if (matcher.find()) {
                int max = Integer.parseInt(matcher.group(2));
                for (int i = 1; i <= max; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(parent.getUrl() + "?page=" + i);
                    webPageEntity.setParsed(false);
                    webPageEntity.setType("productList");
                    LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                    subscriber.onNext(webPageEntity);
                }
            } else {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(parent.getUrl());
                webPageEntity.setParsed(false);
                webPageEntity.setType("productList");
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                subscriber.onNext(webPageEntity);
            }
        }
    }
}
