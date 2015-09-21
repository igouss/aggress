package com.naxsoft.parsers.webPageParsers.marstar;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
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
public class MarstarProductListParser implements WebPageParser {
    private AsyncFetchClient<Set<WebPageEntity>> client;

    public MarstarProductListParser(AsyncFetchClient<Set<WebPageEntity>> client) {
        this.client = client;
    }

    @Override
    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) throws Exception {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        Future<Set<WebPageEntity>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
                if (resp.getStatusCode() == 200) {
                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    System.out.println("Parsing " + document.select("h1").text());
                    Elements elements = document.select("#main-content > div > table > tbody > tr > td > a:nth-child(3)");
                    for (Element e : elements) {
                        WebPageEntity webPageEntity = getProductPage(resp, e);
                        result.add(webPageEntity);
                    }
                    elements = document.select(".baseTable td:nth-child(1) > a");
                    for (Element e : elements) {
                        WebPageEntity webPageEntity = getProductPage(resp, e);
                        result.add(webPageEntity);
                    }
                    elements = document.select("div.subcategoryName a");
                    for (Element e : elements) {
                        WebPageEntity webPageEntity = getProductList(resp, e);
                        result.add(webPageEntity);
                    }
                    if (result.size() == 0) {
                        logger.warn("No entries found url = " + resp.getUri());
                    }
                } else {
                    logger.warn("Failed to open page " + resp.getUri() + " error code: " + resp.getStatusCode());
                }
                return result;
            }

            private WebPageEntity getProductList(Response resp, Element e) {
                String linkUrl = e.attr("abs:href") + "&displayOutOfStock=no";
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(linkUrl);
                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                webPageEntity.setParsed(false);
                webPageEntity.setStatusCode(resp.getStatusCode());
                webPageEntity.setType("productList");
                webPageEntity.setParent(webPage);
                logger.info("Found product list page " + e.text() + " url=" + linkUrl);
                return webPageEntity;
            }

            private WebPageEntity getProductPage(Response resp, Element e) {
                String linkUrl = e.attr("abs:href");
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(linkUrl);
                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                webPageEntity.setParsed(false);
                webPageEntity.setStatusCode(resp.getStatusCode());
                webPageEntity.setType("productPage");
                webPageEntity.setParent(webPage);
                logger.info("Found product " + e.text() + " url=" + linkUrl);
                return webPageEntity;
            }
        });
        // return Observable.defer(() -> Observable.just(future.get()));
        return Observable.defer(() -> Observable.from(future));

    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.marstar.ca/") && webPage.getType().equals("productList");
    }
}
