package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.ning.http.client.AsyncCompletionHandler;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
public class CabelasFrontPageParser implements WebPageParser{

    public Set<WebPageEntity> parse(WebPageEntity webPage) {
        AsyncFetchClient<Set<WebPageEntity>> client = new AsyncFetchClient<>();

        Logger logger = LoggerFactory.getLogger(this.getClass());
        Future<Set<WebPageEntity>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
                if (resp.getStatusCode() == 200) {
                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
//                Elements elements = document.select("a[data-heading=Hunting], a[data-heading=Shooting]");
                    Elements elements = document.select("a[data-heading=Shooting]");

                    for (Element element : elements) {
                        WebPageEntity webPageEntity = new WebPageEntity();
                        webPageEntity.setUrl(element.attr("abs:href"));
                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                        webPageEntity.setParsed(false);
                        webPageEntity.setStatusCode(resp.getStatusCode());
                        webPageEntity.setType("productList");
                        webPageEntity.setParent(webPage);
                        logger.info("parseUrl=" + webPage.getUrl() + ", productListUrl=" + webPageEntity.getUrl());
                        result.add(webPageEntity);
                    }

                }
                return result;
            }
        });
        try {
            Set<WebPageEntity> webPageEntities = future.get();
            client.close();
            return webPageEntities;
        } catch (Exception e) {
            logger.error("HTTP error", e);
            return new HashSet<>();
        }
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.cabelas.ca/") && webPage.getType().equals("frontPage");
    }
}
