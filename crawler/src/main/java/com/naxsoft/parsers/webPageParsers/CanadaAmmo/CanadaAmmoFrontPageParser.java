package com.naxsoft.parsers.webPageParsers.canadaAmmo;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
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
public class CanadaAmmoFrontPageParser implements WebPageParser {
    private AsyncFetchClient<Set<WebPageEntity>> client;

    public CanadaAmmoFrontPageParser(AsyncFetchClient<Set<WebPageEntity>> client) {
        this.client = client;
    }

    @Override
    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) throws Exception {



            Logger logger = LoggerFactory.getLogger(this.getClass());
            Future<Set<WebPageEntity>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
                @Override
                public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                    HashSet<WebPageEntity> result = new HashSet<>();
                    if (resp.getStatusCode() == 200) {
                        Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                        Elements elements = document.select("ul#menu-main-menu:not(.off-canvas-list) > li > a");
                        Logger logger = LoggerFactory.getLogger(this.getClass());
                        logger.info("Parsing for sub-pages + " + webPage.getUrl());

                        for (Element el : elements) {
                            String url = el.attr("abs:href") + "?count=72";
                            try (AsyncFetchClient<Set<WebPageEntity>> client2 = new AsyncFetchClient<>()) {
                                Future<Set<WebPageEntity>> future2 = client2.get(url, new AsyncCompletionHandler<Set<WebPageEntity>>() {
                                    @Override
                                    public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                                        HashSet<WebPageEntity> subResult = new HashSet<>();
                                        Document document = Jsoup.parse(resp.getResponseBody(), url);
                                        Elements elements = document.select("div.clearfix span.pagination a.nav-page");
                                        if (elements.size() == 0) {
                                            WebPageEntity webPageEntity = new WebPageEntity();
                                            webPageEntity.setParent(webPage);
                                            webPageEntity.setUrl(url);
                                            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                            webPage.setStatusCode(resp.getStatusCode());
                                            webPageEntity.setType("productList");
                                            logger.info("productList=" + webPageEntity.getUrl() + ", parent=" + webPage.getUrl());
                                            subResult.add(webPageEntity);
                                        } else {
                                            int i = Integer.parseInt(elements.first().text()) - 1;
                                            int end = Integer.parseInt(elements.last().text());
                                            for (; i <= end; i++) {
                                                WebPageEntity webPageEntity = new WebPageEntity();
                                                webPageEntity.setParent(webPage);
                                                webPageEntity.setUrl(url + "&page=" + i);
                                                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                                webPage.setStatusCode(resp.getStatusCode());
                                                webPageEntity.setType("productList");
                                                logger.info("productList=" + webPageEntity.getUrl() + ", parent=" + webPage.getUrl());
                                                subResult.add(webPageEntity);
                                            }
                                        }
                                        return subResult;
                                    }
                                });
                                result.addAll(future2.get());
                            }
                        }
                    }
                    return result;
                }
            });
        // return Observable.defer(() -> Observable.just(future.get()));
        return Observable.defer(() -> Observable.from(future));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.canadaammo.com/") && webPage.getType().equals("frontPage");
    }
}
