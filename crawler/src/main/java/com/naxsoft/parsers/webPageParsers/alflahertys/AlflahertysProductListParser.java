package com.naxsoft.parsers.webPageParsers.alflahertys;

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

/**
 * Copyright NAXSoft 2015
 */
public class AlflahertysProductListParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(AlflahertysProductListParser.class);
    private final HttpClient client;

    public AlflahertysProductListParser(HttpClient client) {
        this.client = client;
    }

    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.create(subscriber -> {
            client.get(parent.getUrl(), new CompletionHandler<Void>() {
                @Override
                public Void onCompleted(com.ning.http.client.Response resp) throws Exception {
                    if (200 == resp.getStatusCode()) {
                        Document document = Jsoup.parse(resp.getResponseBody(), parent.getUrl());
                        Elements elements = document.select("body > div.container.main.content > div:nth-child(3) a:nth-child(1).view_product_info");

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
                    }
                    subscriber.onCompleted();
                    return null;
                }
            });
        });
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.alflahertys.com/") && webPage.getType().equals("productList");
    }
}
