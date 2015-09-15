package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.WebPageEntity;
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

/**
 * Copyright NAXSoft 2015
 */
public class CabelasFrontPageParser implements WebPageParser{

    public Set<WebPageEntity> parse(WebPageEntity webPage) {
        FetchClient client = new FetchClient();
        HashSet result = new HashSet();
        Logger logger = LoggerFactory.getLogger(this.getClass());

        try {
            Connection.Response resp = client.get(webPage.getUrl());
            if (resp.statusCode() == 200) {
                Document document = Jsoup.parse(resp.body(), webPage.getUrl());
                Elements elements = document.select("a[data-heading=Hunting], a[data-heading=Shooting]");

                for (Element element : elements) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(element.attr("abs:href"));
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setStatusCode(Integer.valueOf(resp.statusCode()));
                    webPageEntity.setType("productList");
                    webPageEntity.setParent(webPage);
                    logger.info("parseUrl=" + webPage.getUrl() + ", productListUrl=" + webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to parse " + webPage.getUrl(), e);
        }

        return result;
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.cabelas.ca/") && webPage.getType().equals("frontPage");
    }
}
