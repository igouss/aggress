package com.naxsoft.parsers.webPageParsers.corwinArms;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class CorwinArmsFrontPageParser implements WebPageParser {
    private AsyncFetchClient client;

    public CorwinArmsFrontPageParser(AsyncFetchClient client) {
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
                    Logger logger = LoggerFactory.getLogger(this.getClass());
                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    Elements elements = document.select("#block-menu-menu-catalogue > div > ul a");
                    for (Element e : elements) {
                        String linkUrl = e.attr("abs:href");

                        WebPageEntity webPageEntity = new WebPageEntity();
                        webPageEntity.setUrl(linkUrl);
                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                        webPageEntity.setParsed(false);
                        webPageEntity.setStatusCode(resp.getStatusCode());
                        webPageEntity.setType("productList");
                        webPageEntity.setParent(webPage);
                        logger.info("Found on front page =" + linkUrl);
                        result.add(webPageEntity);

                    }
                }
                return result;
            }
        });

        return Observable.defer(() ->
                Observable.from(future).
                        flatMap(Observable::from).
                        flatMap(parent -> Observable.from(client.get(parent.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
                            @Override
                            public Set<WebPageEntity> onCompleted(Response resp) throws Exception {
                                HashSet<WebPageEntity> result = new HashSet<>();
                                if (resp.getStatusCode() == 200) {
                                    Document document = Jsoup.parse(resp.getResponseBody(), parent.getUrl());
                                    Elements elements = document.select(".pager li.pager-current");
                                    Matcher matcher = Pattern.compile("(\\d+) of (\\d+)").matcher(elements.text());
                                    if (matcher.find()) {
                                        int max = Integer.parseInt(matcher.group(2));
                                        for (int i = 1; i <= max; i++) {
                                            WebPageEntity webPageEntity = new WebPageEntity();
                                            webPageEntity.setUrl(parent.getUrl() + "?page=" + i);
                                            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                            webPageEntity.setParsed(false);
                                            webPageEntity.setStatusCode(resp.getStatusCode());
                                            webPageEntity.setType("productList");
                                            webPageEntity.setParent(webPage.getParent());
                                            logger.info("Product page listing=" + webPageEntity.getUrl());
                                            result.add(webPageEntity);
                                        }
                                    } else {
                                        WebPageEntity webPageEntity = new WebPageEntity();
                                        webPageEntity.setUrl(parent.getUrl());
                                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                        webPageEntity.setParsed(false);
                                        webPageEntity.setStatusCode(resp.getStatusCode());
                                        webPageEntity.setType("productList");
                                        webPageEntity.setParent(webPage.getParent());
                                        logger.info("Product page listing=" + webPageEntity.getUrl());
                                        result.add(webPageEntity);
                                    }
                                }
                                return result;
                            }
                        }))));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().equals("https://www.corwin-arms.com/") && webPage.getType().equals("frontPage");
    }
}
