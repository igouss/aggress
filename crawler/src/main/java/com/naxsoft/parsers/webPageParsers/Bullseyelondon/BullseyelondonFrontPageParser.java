package com.naxsoft.parsers.webPageParsers.bullseyelondon;

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

import java.sql.Timestamp;

public class BullseyelondonFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(BullseyelondonFrontPageParser.class);
    private final HttpClient client;

    public BullseyelondonFrontPageParser(HttpClient client) {
        this.client = client;
    }

    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.create(subscriber -> client.get(webPage.getUrl(), new CompletionHandler<Void>() {
            @Override
            public Void onCompleted(com.ning.http.client.Response resp) throws Exception {
                if (200 == resp.getStatusCode()) {
                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    Elements elements = document.select(".vertnav-cat a");
                    for (Element element : elements) {
                        WebPageEntity webPageEntity = new WebPageEntity();
                        webPageEntity.setUrl(element.attr("abs:href") + "?limit=all");
                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                        webPageEntity.setParsed(false);
                        webPageEntity.setType("productList");
                        LOGGER.info("productList = {}, parent = {}", webPageEntity.getUrl(), webPage.getUrl());
                        subscriber.onNext(webPageEntity);
                    }
                }
                subscriber.onCompleted();
                return null;
            }
        }));

    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.bullseyelondon.com/") && webPage.getType().equals("frontPage");
    }
}
