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
public class CanadaAmmoProductListParser implements WebPageParser {
    @Override
    public Set<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        Set<WebPageEntity> result = new HashSet<>();
        FetchClient fetchClient = new FetchClient();
        Connection.Response response = fetchClient.get(webPage.getUrl());
        Document document = Jsoup.parse(response.body());
        Elements elements = document.select("a.product__link");
        for (Element element : elements) {
            WebPageEntity webPageEntity = new WebPageEntity();
            webPageEntity.setUrl(element.attr("abs:href"));
            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
            webPageEntity.setParsed(false);
            webPageEntity.setType("productPage");
            webPageEntity.setParent(webPage);
            logger.info("productPageUrl=" + webPageEntity.getUrl() + ", " + "parseUrl=" + webPage.getUrl());
            result.add(webPageEntity);
        }
        return result;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.canadaammo.com/") && webPage.getType().equals("productList");
    }
}
