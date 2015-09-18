package com.naxsoft.parsers.webPageParsers.alflahertys;

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

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
public class AlflahertysFrontPageParser implements WebPageParser {
    @Override
    public Set<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        try(AsyncFetchClient<Set<WebPageEntity>> client = new AsyncFetchClient<>()) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            Future<Set<WebPageEntity>> future = client.get("http://www.alflahertys.com/collections/all/", new AsyncCompletionHandler<Set<WebPageEntity>>() {
                @Override
                public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                    HashSet<WebPageEntity> result = new HashSet<>();
                    if (resp.getStatusCode() == 200) {

                        Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                        Elements elements = document.select(".paginate a");
                        int max = 0;
                        for (Element element : elements) {
                            try {
                                int tmp = Integer.parseInt(element.text());
                                if (tmp > max) {
                                    max = tmp;
                                }
                            } catch (NumberFormatException e) {
                                // ignore
                            }
                        }
                        for (int i = 1; i <= max; i++) {
                            WebPageEntity webPageEntity = new WebPageEntity();
                            webPageEntity.setUrl("http://www.alflahertys.com/collections/all?page=" + i);
                            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                            webPageEntity.setParsed(false);
                            webPageEntity.setStatusCode(resp.getStatusCode());
                            webPageEntity.setType("productList");
                            webPageEntity.setParent(webPage);
                            logger.info("productList = " + webPageEntity.getUrl() + ", parent = " + webPage.getUrl());
                            result.add(webPageEntity);
                        }
                    }
                    return result;
                }
            });
            try {
                Set<WebPageEntity> webPageEntities = future.get();
                return webPageEntities;
            } catch (Exception e) {
                logger.error("HTTP error", e);
                return new HashSet<>();
            }
        }
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.alflahertys.com/") && webPage.getType().equals("frontPage");
    }
}
