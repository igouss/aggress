package com.naxsoft.parsers.webPageParsers.theammosource;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.ning.http.client.AsyncCompletionHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

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
    public Observable<Set<WebPageEntity>> parse(WebPageEntity parent) throws Exception {
        Future<Set<WebPageEntity>> future = client.get(parent.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
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
                            logger.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), parent.getUrl());
                            result.add(webPageEntity);
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
                                logger.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), parent.getUrl());
                                result.add(webPageEntity);
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
                            result.add(webPageEntity);
                        }
                    }
                }
                return result;
            }
        });
        return Observable.from(future);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.theammosource.com/") && webPage.getType().equals("productList");
    }
}