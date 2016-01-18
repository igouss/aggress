package com.naxsoft.parsers.webPageParsers.ellwoodepps;

import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
public class EllwoodeppsFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(EllwoodeppsFrontPageParser.class);

    public EllwoodeppsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("https://ellwoodepps.com/hunting/firearms.html?limit=100&no_cache=true", parent));

        return Observable.create(subscriber -> Observable.just(webPageEntities).
                flatMap(Observable::from).
                flatMap(page -> Observable.from(client.get(page.getUrl(), new VoidCompletionHandler(page, subscriber)))));
    }

    private static WebPageEntity create(String url, WebPageEntity parent) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://ellwoodepps.com/") && webPage.getType().equals("frontPage");
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
                if (parseDocument(document)) return null;
            }
            subscriber.onCompleted();
            return null;
        }

        private boolean parseDocument(Document document) {
            String elements = document.select("div.amount-container > p").text();
            Matcher matcher = Pattern.compile("of\\s(\\d+)").matcher(elements);
            if (!matcher.find()) {
                LOGGER.error("Unable to parse total pages");
                subscriber.onCompleted();
                return true;
            }

            int productTotal = Integer.parseInt(matcher.group(1));
            int pageTotal = (int) Math.ceil(productTotal / 100.0);

            for (int i = 1; i <= pageTotal; i++) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(page.getUrl() + "&p=" + i);
                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                webPageEntity.setParsed(false);
                webPageEntity.setType("productList");
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                subscriber.onNext(webPageEntity);
            }
            return false;
        }
    }
}