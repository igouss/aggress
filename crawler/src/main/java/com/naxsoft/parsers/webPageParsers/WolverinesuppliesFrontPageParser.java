package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.ning.http.client.AsyncCompletionHandler;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

public class WolverinesuppliesFrontPageParser implements WebPageParser {
    public Set<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        AsyncFetchClient<Set<WebPageEntity>> client = new AsyncFetchClient<>();

        Logger logger = LoggerFactory.getLogger(this.getClass());
        Future<Set<WebPageEntity>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
                if (resp.getStatusCode() == 200) {
                    Logger logger = LoggerFactory.getLogger(this.getClass());
                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    Elements elements = document.select(".mainnav a");
                    for (Element e : elements) {
                        String linkUrl = e.attr("abs:href");
                        if (null != linkUrl && !linkUrl.isEmpty() && linkUrl.contains("Products") && e.siblingElements().size() == 0) {
                            WebPageEntity webPageEntity = new WebPageEntity();
                            webPageEntity.setUrl(linkUrl);
                            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                            webPageEntity.setParsed(false);
                            webPageEntity.setStatusCode(resp.getStatusCode());
                            webPageEntity.setType("productList");
                            webPageEntity.setParent(webPage);
                            logger.info("ProductPageUrl=" + linkUrl + ", " + "parseUrl=" + webPage.getUrl());
                            result.add(webPageEntity);
                        }
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
        return webPage.getUrl().equals("https://www.wolverinesupplies.com/") && webPage.getType().equals("frontPage");
    }
}
