package com.naxsoft.parsers.webPageParsers.irunguns;

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
public class IrungunsFrontPageParser implements WebPageParser {
    @Override
    public Set<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        try (AsyncFetchClient<Set<WebPageEntity>> client = new AsyncFetchClient<>()) {

            Logger logger = LoggerFactory.getLogger(this.getClass());
            Future<Set<WebPageEntity>> future = client.get("https://www.irunguns.us/product_categories.php", new AsyncCompletionHandler<Set<WebPageEntity>>() {
                @Override
                public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                    HashSet<WebPageEntity> result = new HashSet<>();
                    if (resp.getStatusCode() == 200) {
                        Logger logger = LoggerFactory.getLogger(this.getClass());
                        Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                        Elements elements = document.select("#content .widthLimit a");
                        for (Element e : elements) {
                            String linkUrl = e.attr("abs:href");
                            WebPageEntity webPageEntity = new WebPageEntity();
                            webPageEntity.setUrl(linkUrl);
                            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                            webPageEntity.setParsed(false);
                            webPageEntity.setStatusCode(resp.getStatusCode());
                            webPageEntity.setType("productPage");
                            webPageEntity.setParent(webPage);
                            logger.info("ProductPageUrl=" + linkUrl + ", " + "parseUrl=" + webPage.getUrl());
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
        return webPage.getUrl().equals("https://www.irunguns.us/") && webPage.getType().equals("frontPage");
    }
}
