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
public class BullseyelondonFrontPageParser implements WebPageParser {

    @Override
    public Set<WebPageEntity> parse(String url) {
        FetchClient client = new FetchClient();
        Set<WebPageEntity> result = new HashSet<>();
        Logger logger = LoggerFactory.getLogger(BullseyelondonFrontPageParser.class);
        try {
            Connection.Response response = client.get(url);
            if (response.statusCode() == 200) {
                Document document = Jsoup.parse(response.body(), response.charset());
                Elements elements = document.select(".vertnav-cat a");
                for (Element e : elements) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(e.attr("abs:href") + "?limit=all");
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setStatusCode(response.statusCode());
                    webPageEntity.setType("productList");
                    logger.info("parseUrl=" + url + ", productListUrl=" + webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public boolean canParse(String url, String action) {
        return url.startsWith("http://www.bullseyelondon.com/") && action.equals("frontPage");
    }
}
