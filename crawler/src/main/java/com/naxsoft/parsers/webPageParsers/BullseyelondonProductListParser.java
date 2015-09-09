//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.WebPageEntity;
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

public class BullseyelondonProductListParser implements WebPageParser {

    public Set<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        FetchClient client = new FetchClient();
        HashSet result = new HashSet();
        Logger logger = LoggerFactory.getLogger(this.getClass());

            Response e = client.get(webPage.getUrl());
            if(e.statusCode() == 200) {

                Document document = Jsoup.parse(e.body(), webPage.getUrl());
                Elements elements = document.select(".item .product-name a");

                for (Element element : elements) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(element.attr("abs:href"));
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setStatusCode(Integer.valueOf(e.statusCode()));
                    webPageEntity.setType("productPage");
                    webPageEntity.setParent(webPage);
                    logger.info("productPageUrl=" + webPageEntity.getUrl() + ", " + "parseUrl=" + webPage.getUrl());
                    result.add(webPageEntity);
                }
            }


        return result;
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.bullseyelondon.com/") && webPage.getType().equals("productList");
    }
}
