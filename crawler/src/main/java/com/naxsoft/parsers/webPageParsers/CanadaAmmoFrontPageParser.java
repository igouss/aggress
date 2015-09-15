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

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class CanadaAmmoFrontPageParser implements WebPageParser {
    @Override
    public Set<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        Set<WebPageEntity> result = new HashSet<>();
        FetchClient fetchClient = new FetchClient();
        Connection.Response response = fetchClient.get(webPage.getUrl());
        Document document = Jsoup.parse(response.body(), webPage.getUrl());
        Elements elements = document.select("ul#menu-main-menu:not(.off-canvas-list) > li > a");
        Logger logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Parsing for sub-pages + " + webPage.getUrl());

        for (Element el : elements) {
            String url = el.attr("abs:href") + "?count=72";
            response = fetchClient.get(url);
            document = Jsoup.parse(response.body(), url);
            elements = document.select("div.clearfix span.pagination a.nav-page");
            if (elements.size() == 0) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setParent(webPage);
                webPageEntity.setUrl(url);
                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                webPageEntity.setType("productList");
                logger.info("Found productList page + " + webPageEntity.getUrl());
                result.add(webPageEntity);
            } else {
                int i = Integer.parseInt(elements.first().text()) - 1;
                int end = Integer.parseInt(elements.last().text());
                for (; i <= end; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setParent(webPage);
                    webPageEntity.setUrl(url + "&page=" + i);
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setType("productList");
                    logger.info("Found productList page + " + webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            }

        }
        return result;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.canadaammo.com/") && webPage.getType().equals("frontPage");
    }
}
