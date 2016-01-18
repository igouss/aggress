package com.naxsoft.parsers.webPageParsers.dantesports;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class DantesportsProductListParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(DantesportsProductListParser.class);

    public DantesportsProductListParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return Observable.create(subscriber -> client.get(parent.getUrl(), new CompletionHandler<Void>() {
            @Override
            public Void onCompleted(com.ning.http.client.Response resp) throws Exception {
                if (200 == resp.getStatusCode()) {
                    Document document = Jsoup.parse(resp.getResponseBody(), parent.getUrl());
                    Elements elements = document.select("#store div.listItem");

                    for (Element element : elements) {
                        String onclick = element.attr("onclick");
                        Matcher matcher = Pattern.compile("\\d+").matcher(onclick);
                        if (matcher.find()) {
                            WebPageEntity webPageEntity = new WebPageEntity();
                            webPageEntity.setUrl("https://shop.dantesports.com/items_detail.php?iid=" + matcher.group());
                            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                            webPageEntity.setParsed(false);
                            webPageEntity.setType("productPage");
                            webPageEntity.setCategory(parent.getCategory());
                            LOGGER.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), parent.getUrl());
                            subscriber.onNext(webPageEntity);
                        } else {
                            LOGGER.info("Product id not found: {}", parent);
                        }
                    }
                }
                subscriber.onCompleted();
                return null;
            }
        }));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://shop.dantesports.com/") && webPage.getType().equals("productList");
    }
}
