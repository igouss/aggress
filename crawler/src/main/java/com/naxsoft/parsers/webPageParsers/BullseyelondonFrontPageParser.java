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

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class BullseyelondonFrontPageParser implements WebPageParser {

    public Set<WebPageEntity> parse(String url) {
        FetchClient client = new FetchClient();
        HashSet result = new HashSet();
        Logger logger = LoggerFactory.getLogger(BullseyelondonFrontPageParser.class);

        try {
            Response e = client.get(url);
            if (e.statusCode() == 200) {
                Document document = Jsoup.parse(e.body(), url);
                Elements elements = document.select(".vertnav-cat a");
                Iterator var8 = elements.iterator();

                while (var8.hasNext()) {
                    Element e1 = (Element) var8.next();
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(e1.attr("abs:href") + "?limit=all");
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setStatusCode(Integer.valueOf(e.statusCode()));
                    webPageEntity.setType("productList");
                    logger.info("Thread " + Thread.currentThread().toString() + " parseUrl=" + url + ", productListUrl=" + webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            }
        } catch (IOException var11) {
            var11.printStackTrace();
        }

        return result;
    }

    public boolean canParse(String url, String action) {
        return url.startsWith("http://www.bullseyelondon.com/") && action.equals("frontPage");
    }
}
