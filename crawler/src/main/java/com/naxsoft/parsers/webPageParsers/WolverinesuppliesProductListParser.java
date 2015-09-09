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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WolverinesuppliesProductListParser implements WebPageParser {


    public Set<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        FetchClient client = new FetchClient();
        Set<WebPageEntity> result = new HashSet<>();
        Response response = client.get(webPage.getUrl());
        if (response.statusCode() == 200) {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            Document document = Jsoup.parse(response.body(), webPage.getUrl());
            Elements elements = document.select("div[ng-init]");

            for (Element e : elements) {
                String linkUrl = e.attr("ng-init");
                Matcher categoryMatcher = Pattern.compile("\'\\d+\'").matcher(linkUrl);

                if (categoryMatcher.find()) {
                    String productCategory = categoryMatcher.group();
                    String productDetailsUrl = "https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetJSONItems?data={\"WordList\":\"\",\"ItemNumber\":\"\",\"CategoryCode\":" + productCategory + ",\"SearchMethod\":\"Category\",\"Limit\":0}";
                    String productDetailsJson = client.get(productDetailsUrl).body();
                    Matcher itemNumberMatcher = Pattern.compile("ItemNumber\":\"(\\w+|\\d+)\"").matcher(productDetailsJson);
                    StringBuilder sb = new StringBuilder();

                    while (itemNumberMatcher.find()) {
                        logger.info(itemNumberMatcher.group(1));
                        sb.append(itemNumberMatcher.group(1));
                        sb.append(',');
                    }

                    if (0 != sb.length()) {
                        WebPageEntity webPageEntity = new WebPageEntity();
                        webPageEntity.setUrl("https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetItemsData?ItemNumbersString=" + sb.toString());
                        webPageEntity.setParent(webPage.getParent());
                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                        webPageEntity.setType("productPage");
                        webPageEntity.setParent(webPage);
                        result.add(webPageEntity);
                    }
                }
            }
        }
        return result;
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.wolverinesupplies.com/") && webPage.getType().equals("productList");
    }
}
