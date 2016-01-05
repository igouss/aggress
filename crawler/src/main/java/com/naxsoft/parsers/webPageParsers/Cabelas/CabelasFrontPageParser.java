package com.naxsoft.parsers.webPageParsers.cabelas;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.CompletionHandler;
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

/**
 * Copyright NAXSoft 2015
 */
public class CabelasFrontPageParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(CabelasFrontPageParser.class);
    private final AsyncFetchClient client;

    public CabelasFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) {
        return Observable.from(client.get(webPage.getUrl(), new CompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
                if (200 == resp.getStatusCode()) {
                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    Elements elements = document.select("a[data-heading=Shooting]");

                    for (Element element : elements) {
                        WebPageEntity webPageEntity = new WebPageEntity();
                        webPageEntity.setUrl(element.attr("abs:href"));
                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                        webPageEntity.setParsed(false);
                        webPageEntity.setStatusCode(resp.getStatusCode());
                        webPageEntity.setType("productList");
                        logger.info("productList={}, parent={}", webPageEntity.getUrl(), webPage.getUrl());
                        result.add(webPageEntity);
                    }

                }
                return result;
            }
        }));
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.cabelas.ca/") && webPage.getType().equals("frontPage");
    }
}
