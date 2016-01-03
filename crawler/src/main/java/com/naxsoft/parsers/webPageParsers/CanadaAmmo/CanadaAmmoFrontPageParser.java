package com.naxsoft.parsers.webPageParsers.canadaAmmo;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
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
public class CanadaAmmoFrontPageParser extends AbstractWebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(CanadaAmmoFrontPageParser.class);

    public CanadaAmmoFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) throws Exception {
        Future<Set<String>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<String>>() {
            @Override
            public HashSet<String> onCompleted(com.ning.http.client.Response resp) throws Exception {
                HashSet<String> result = new HashSet<>();
                if (200 == resp.getStatusCode()) {
                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    Elements elements = document.select("ul#menu-main-menu:not(.off-canvas-list) > li > a");
                    logger.info("Parsing for sub-pages + {}", webPage.getUrl());

                    for (Element el : elements) {
                        String url = el.attr("abs:href") + "?count=72";
                        result.add(url);
                    }
                }
                return result;
            }
        });

        return Observable.from(future).flatMap(Observable::from).flatMap(url -> {
                    Future<Set<WebPageEntity>> setFuture = client.get(url, new AsyncCompletionHandler<Set<WebPageEntity>>() {
                        @Override
                        public Set<WebPageEntity> onCompleted(Response resp) throws Exception {
                            HashSet<WebPageEntity> subResult = new HashSet<>();
                            Document document = Jsoup.parse(resp.getResponseBody(), url);
                            Elements elements = document.select("div.clearfix span.pagination a.nav-page");
                            if (elements.isEmpty()) {
                                WebPageEntity webPageEntity = new WebPageEntity();
                                webPageEntity.setUrl(url);
                                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                webPage.setStatusCode(resp.getStatusCode());
                                webPageEntity.setType("productList");
                                logger.info("productList={}, parent={}", webPageEntity.getUrl(), webPage.getUrl());
                                subResult.add(webPageEntity);
                            } else {
                                int i = Integer.parseInt(elements.first().text()) - 1;
                                int end = Integer.parseInt(elements.last().text());
                                for (; i <= end; i++) {
                                    WebPageEntity webPageEntity = new WebPageEntity();
                                    webPageEntity.setUrl(url + "&page=" + i);
                                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                    webPage.setStatusCode(resp.getStatusCode());
                                    webPageEntity.setType("productList");
                                    logger.info("productList={}, parent={}", webPageEntity.getUrl(), webPage.getUrl());
                                    subResult.add(webPageEntity);
                                }
                            }
                            return subResult;
                        }
                    });
                    return Observable.from(setFuture);
                }
        );
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.canadaammo.com/") && webPage.getType().equals("frontPage");
    }
}
