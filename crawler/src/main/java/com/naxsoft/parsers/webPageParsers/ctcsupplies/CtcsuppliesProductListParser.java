package com.naxsoft.parsers.webPageParsers.ctcsupplies;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.crawler.CompletionHandler;
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

/**
 * Copyright NAXSoft 2015
 */
public class CtcsuppliesProductListParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger logger = LoggerFactory.getLogger(CtcsuppliesProductListParser.class);

    public CtcsuppliesProductListParser(HttpClient client) {
        this.client = client;
    }

    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.create(subscriber -> {
            client.get(parent.getUrl(), new CompletionHandler<Void>() {
                @Override
                public Void onCompleted(com.ning.http.client.Response resp) throws Exception {
                    if (200 == resp.getStatusCode()) {
                        Document document = Jsoup.parse(resp.getResponseBody(), parent.getUrl());
                        Elements elements = document.select("a.grid-link");

                        for (Element element : elements) {
                            if (!element.select("span.badge.badge--sold-out").isEmpty()) {
                                continue;
                            }
                            WebPageEntity webPageEntity = new WebPageEntity();
                            webPageEntity.setUrl(element.attr("abs:href"));
                            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                            webPageEntity.setParsed(false);
                            webPageEntity.setStatusCode(resp.getStatusCode());
                            webPageEntity.setType("productPage");
                            webPageEntity.setCategory(parent.getCategory());
                            logger.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), parent.getUrl());
                            subscriber.onNext(webPageEntity);
                        }
                    }
                    subscriber.onCompleted();
                    return null;
                }
            });
        });
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://ctcsupplies.ca/") && webPage.getType().equals("productList");
    }
}
