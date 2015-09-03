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
public class BullseyelondonProductListParser implements WebPageParser {
    @Override
    public Set<WebPageEntity> parse(String url) {
        FetchClient client = new FetchClient();
        Set<WebPageEntity> result = new HashSet<>();

        try {
            Connection.Response response = client.get(url);
            if (response.statusCode() == 200) {
                Logger logger = LoggerFactory.getLogger(BullseyelondonProductListParser.class);
                Document document = Jsoup.parse(response.body(), response.charset());
                Elements elements = document.select(".item .product-name a");
                for (Element e : elements) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(e.attr("abs:href"));
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setStatusCode(response.statusCode());
                    webPageEntity.setType("productPage");
                    logger.info("productPageUrl=" + webPageEntity.getUrl() + ", " + "parseUrl=" + url);
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
        return url.startsWith("http://www.bullseyelondon.com/") && action.equals("productList");
    }
}
