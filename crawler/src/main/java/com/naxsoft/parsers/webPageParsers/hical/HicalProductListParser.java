package com.naxsoft.parsers.webPageParsers.hical;

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
public class HicalProductListParser extends AbstractWebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(HicalProductListParser.class);

    public HicalProductListParser(AsyncFetchClient client) {
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
                    Elements elements = document.select("#frmCompare .ProductDetails a");
                    for (Element el : elements) {
                        WebPageEntity webPageEntity = new WebPageEntity();
                        webPageEntity.setUrl(el.attr("abs:href"));
                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                        webPageEntity.setParsed(false);
                        webPageEntity.setStatusCode(resp.getStatusCode());
                        webPageEntity.setType("productPage");
                        logger.info("Product page listing={}", webPageEntity.getUrl());
                        result.add(webPageEntity);
                    }
                }
                return result;
            }
        });
        return Observable.from(future);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.hical.ca/") && webPage.getType().equals("productList");
    }
}