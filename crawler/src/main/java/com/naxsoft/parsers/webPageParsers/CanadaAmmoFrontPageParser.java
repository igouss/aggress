package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.WebPageEntity;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
        Document document = Jsoup.parse(response.body());
        Elements elements = document.select("ul#menu-main-menu:not(.off-canvas-list) > li > a");

        for(Element el : elements) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setParent(webPage);
            webPageEntity.setUrl(el.attr("abs:href") + "?count=72");
            webPageEntity.setType("productList");
            webPageEntity.setParsed(false);
            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.canadaammo.com/") && webPage.getType().equals("frontPage");
    }
}
