//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BullseyelondonProductListParser implements WebPageParser {
    public BullseyelondonProductListParser() {
    }

    public Set<WebPageEntity> parse(String url) {
        FetchClient client = new FetchClient();
        HashSet result = new HashSet();

        try {
            Response e = client.get(url);
            if(e.statusCode() == 200) {
                Logger logger = LoggerFactory.getLogger(BullseyelondonProductListParser.class);
                Document document = Jsoup.parse(e.body(), url);
                Elements elements = document.select(".item .product-name a");
                Iterator var8 = elements.iterator();

                while(var8.hasNext()) {
                    Element e1 = (Element)var8.next();
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(e1.attr("abs:href"));
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setStatusCode(Integer.valueOf(e.statusCode()));
                    webPageEntity.setType("productPage");
                    logger.info("productPageUrl=" + webPageEntity.getUrl() + ", " + "parseUrl=" + url);
                    result.add(webPageEntity);
                }
            }
        } catch (IOException var11) {
            var11.printStackTrace();
        }

        return result;
    }

    public boolean canParse(String url, String action) {
        return url.startsWith("http://www.bullseyelondon.com/") && action.equals("productList");
    }
}
