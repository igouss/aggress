package com.naxsoft.parsers.webPageParsers.ctcsupplies;

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
public class CtcsuppliesProductListParser implements WebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(CtcsuppliesProductListParser.class);

    public CtcsuppliesProductListParser(AsyncFetchClient client) {
        this.client = client;
    }

    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) throws Exception {

        Future<Set<WebPageEntity>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
                if (200 == resp.getStatusCode()) {
                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    Elements elements = document.select("a.grid-link");

                    for (Element element : elements) {
                        if (!element.select("span.badge.badge--sold-out").isEmpty()) {
                            continue;
                        }
                        WebPageEntity webPageEntity = new WebPageEntity();
                        webPageEntity.setUrl(element.attr("abs:href"));
                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                        webPageEntity.setParsed(false);
                        webPageEntity.setStatusCode(resp.getStatusCode());
                        webPageEntity.setType("productPage");
                        webPageEntity.setParent(webPage);
                        logger.info("productPageUrl={}, parseUrl={}", webPageEntity.getUrl(), webPage.getUrl());
                        result.add(webPageEntity);
                    }
                }
                return result;
            }
        });
        return Observable.from(future);
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://ctcsupplies.ca/") && webPage.getType().equals("productList");
    }
}
