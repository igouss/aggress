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
    public Set<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        FetchClient client = new FetchClient();
        Set<WebPageEntity> result = new HashSet<>();
        Response response = client.get(webPage.getUrl());
        if(response.statusCode() == 200) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            Document document = Jsoup.parse(response.body(), webPage.getUrl());
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
                    webPageEntity.setParent(webPage);
                    logger.info("ProductPageUrl=" + linkUrl + ", " + "parseUrl=" + webPage.getUrl());
                    result.add(webPageEntity);
                }
            }
        }

        return result;
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().equals("https://www.wolverinesupplies.com/") && webPage.getType().equals("frontPage");
    }
}
