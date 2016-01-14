package com.naxsoft.parsers.webPageParsers.tradeexcanada;

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
public class TradeexCanadaProductListParser extends AbstractWebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(TradeexCanadaProductListParser.class);

    public TradeexCanadaProductListParser(AsyncFetchClient client) {
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
                        if (parent.getUrl().contains("page=")) {
                            Elements elements = document.select(".view-content a");
                            for (Element element : elements) {
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
                        } else {
                            Elements subPages = document.select(".pager a");
                            for (Element subPage : subPages) {
                                subscriber.onNext(create(subPage.attr("abs:href")));
                            }
                            subscriber.onNext(create(parent.getUrl() + "?page=0"));
                        }
                    }
                    subscriber.onCompleted();
                    return null;
                }
            });
        });
    }

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
        webPageEntity.setParsed(false);
        webPageEntity.setStatusCode(200);
        webPageEntity.setType("productList");
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.tradeexcanada.com/") && webPage.getType().equals("productList");
    }
}