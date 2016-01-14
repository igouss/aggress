package com.naxsoft.parsers.webPageParsers.theammosource;

import com.naxsoft.crawler.AsyncFetchClient;
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
public class TheammosourceProductListParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(TheammosourceProductListParser.class);
    private final AsyncFetchClient client;

    public TheammosourceProductListParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.create(subscriber -> {
            client.get(parent.getUrl(), new CompletionHandler<Void>() {
                @Override
                public Void onCompleted(com.ning.http.client.Response resp) throws Exception {
                    if (200 == resp.getStatusCode()) {
                        Document document = Jsoup.parse(resp.getResponseBody(), parent.getUrl());
                        Elements elements = document.select(".categoryListBoxContents > a");
                        if (!elements.isEmpty()) {
                            for (Element element : elements) {
                                WebPageEntity webPageEntity = new WebPageEntity();
                                webPageEntity.setUrl(element.attr("abs:href"));
                                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                webPageEntity.setParsed(false);
                                webPageEntity.setStatusCode(resp.getStatusCode());
                                webPageEntity.setType("productList");
                                webPageEntity.setCategory(parent.getCategory());
                                logger.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), parent.getUrl());
                                subscriber.onNext(webPageEntity);
                            }
                        } else {
                            if (!parent.getUrl().contains("&page=")) {
                                elements = document.select("#productsListingListingTopLinks > a");
                                for (Element element : elements) {
                                    WebPageEntity webPageEntity = new WebPageEntity();
                                    webPageEntity.setUrl(element.attr("abs:href"));
                                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                    webPageEntity.setParsed(false);
                                    webPageEntity.setStatusCode(resp.getStatusCode());
                                    webPageEntity.setType("productList");
                                    webPageEntity.setCategory(parent.getCategory());
                                    logger.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), parent.getUrl());
                                    subscriber.onNext(webPageEntity);
                                }
                            }
                            elements = document.select(".itemTitle a");
                            for (Element element : elements) {
                                WebPageEntity webPageEntity = new WebPageEntity();
                                webPageEntity.setUrl(element.attr("abs:href"));
                                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                webPageEntity.setParsed(false);
                                webPageEntity.setStatusCode(resp.getStatusCode());
                                webPageEntity.setType("productPage");
                                logger.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), parent.getUrl());
                                subscriber.onNext(webPageEntity);
                            }
                        }
                    }
                    subscriber.onCompleted();
                    return null;
                }
            });
        });
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.theammosource.com/") && webPage.getType().equals("productList");
    }
}