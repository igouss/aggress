package com.naxsoft.parsers.webPageParsers.tradeexcanada;

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
public class TradeexCanadaFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeexCanadaFrontPageParser.class);

    public TradeexCanadaFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("https://www.tradeexcanada.com/products_list"));

        return Observable.create(subscriber -> {
           for(WebPageEntity webPageEntity : webPageEntities) {
               client.get(webPageEntity.getUrl(), new CompletionHandler(webPageEntity, subscriber));
           }
        });
    }

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        webPageEntity.setCategory("n/a");
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.tradeexcanada.com/") && webPage.getType().equals("frontPage");
    }

    private static class CompletionHandler extends AbstractCompletionHandler {
        private final WebPageEntity page;
        private final Subscriber<? super WebPageEntity> subscriber;

        public CompletionHandler(WebPageEntity page, Subscriber<? super WebPageEntity> subscriber) {
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
            Elements elements = document.select(".view-content a");
            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(element.attr("abs:href"));
                webPageEntity.setParsed(false);
                webPageEntity.setType("productList");

                if (element.text().contains("AAA Super Specials")) {
                    webPageEntity.setCategory("firearms,ammo");
                }  else if (element.text().contains("Combination Guns")) {
                    webPageEntity.setCategory("firearms");
                } else if (element.text().contains("Double Rifles")) {
                    webPageEntity.setCategory("firearms");
                } else if (element.text().contains("Handguns")) {
                    webPageEntity.setCategory("firearms");
                } else if (element.text().contains("Hunting and Sporting Arms")) {
                    webPageEntity.setCategory("firearms");
                } else if (element.text().contains("Rifle")) {
                    webPageEntity.setCategory("firearms");
                } else if (element.text().contains("Shotguns")) {
                    webPageEntity.setCategory("firearms");
                } else if (element.text().contains("Ammunition")) {
                    webPageEntity.setCategory("ammo");
                } else if (element.text().contains("Ammunition")) {
                    webPageEntity.setCategory("Reloading Components");
                } else if (element.text().contains("Scopes")) {
                    webPageEntity.setCategory("optics");
                } else {
                    webPageEntity.setCategory("misc");
                }

                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                subscriber.onNext(webPageEntity);
            }
        }
    }
}