//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
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

public class WolverinesuppliesFrontPageParser implements WebPageParser {
    public WolverinesuppliesFrontPageParser() {
    }

    public Set<WebPageEntity> parse(String url) throws Exception {
        FetchClient client = new FetchClient();
        HashSet result = new HashSet();
        Response response = client.get(url);
        if(response.statusCode() == 200) {
            Logger logger = LoggerFactory.getLogger(WolverinesuppliesFrontPageParser.class);
            Document document = Jsoup.parse(response.body(), url);
            Elements elements = document.select(".mainnav a");
            Iterator var8 = elements.iterator();

            while(var8.hasNext()) {
                Element e = (Element)var8.next();
                String linkUrl = e.attr("abs:href");
                if(null != linkUrl && !linkUrl.isEmpty() && linkUrl.contains("Products") && e.siblingElements().size() == 0) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(linkUrl);
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setStatusCode(Integer.valueOf(response.statusCode()));
                    webPageEntity.setType("productPage");
                    logger.info("productPageUrl=" + webPageEntity.getUrl() + ", " + "parseUrl=" + url);
                    result.add(webPageEntity);
                }
            }
        }

        return result;
    }

    public boolean canParse(String url, String action) {
        return url.equals("https://www.wolverinesupplies.com/") && action.equals("frontPage");
    }
}
