package com.naxsoft.parsers.webPageParsers.dantesports;

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
import java.util.LinkedList;

/**
 * Copyright NAXSoft 2015
 */
public class DantesportsFrontPageParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(DantesportsFrontPageParser.class);
    private final AsyncFetchClient client;

    public DantesportsFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }


    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.create(subscriber -> {
            Observable.from(client.get("https://shop.dantesports.com/set_lang.php?lang=EN", new LinkedList<>(), getCookiesHandler(), false)).subscribe(cookies -> {
                client.get(webPage.getUrl(), cookies, new CompletionHandler<Void>() {
                    @Override
                    public Void onCompleted(com.ning.http.client.Response resp) throws Exception {
                        if (200 == resp.getStatusCode()) {
                            Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                            Elements elements = document.select("#scol1 > div.scell_menu > li > a");

                            for (Element element : elements) {
                                WebPageEntity webPageEntity = new WebPageEntity();
                                webPageEntity.setUrl(element.attr("abs:href") + "&paging=0");
                                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                webPageEntity.setParsed(false);
                                webPageEntity.setStatusCode(resp.getStatusCode());
                                webPageEntity.setType("productList");
                                logger.info("productList={}, parent={}", webPageEntity.getUrl(), webPage.getUrl());
                                subscriber.onNext(webPageEntity);
                            }
                        }
                        subscriber.onCompleted();
                        return null;
                    }
                });
            });
        });
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://shop.dantesports.com/") && webPage.getType().equals("frontPage");
    }
}
