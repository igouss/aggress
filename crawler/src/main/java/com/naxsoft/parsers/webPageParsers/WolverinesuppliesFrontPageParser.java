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

public class WolverinesuppliesFrontPageParser implements WebPageParser {
    public Set<WebPageEntity> parse(String url) throws Exception {
        FetchClient client = new FetchClient();
        Set<WebPageEntity> result = new HashSet<>();
        Response response = client.get(url);
        if(response.statusCode() == 200) {
            Logger logger = LoggerFactory.getLogger(WolverinesuppliesFrontPageParser.class);
            Document document = Jsoup.parse(response.body(), url);
            Elements elements = document.select(".mainnav a");
            for (Element e : elements) {
                String linkUrl = e.attr("abs:href");
                if (null != linkUrl && !linkUrl.isEmpty() && linkUrl.contains("Products") && e.siblingElements().size() == 0) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(linkUrl);
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setStatusCode(response.statusCode());
                    webPageEntity.setType("productList");
                    logger.info("Thread " + Thread.currentThread().toString() + " productPageUrl=" + webPageEntity.getUrl() + ", " + "parseUrl=" + url);
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
