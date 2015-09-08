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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WolverinesuppliesProductListParser implements WebPageParser {
    public WolverinesuppliesProductListParser() {
    }

    public Set<WebPageEntity> parse(String url) throws Exception {
        FetchClient client = new FetchClient();
        HashSet result = new HashSet();
        Response response = client.get(url);
        if(response.statusCode() == 200) {
            Logger logger = LoggerFactory.getLogger(WolverinesuppliesProductListParser.class);
            Document document = Jsoup.parse(response.body(), url);
            Elements elements = document.select("div[ng-init]");
            Iterator var8 = elements.iterator();

            while(true) {
                Matcher matcher;
                do {
                    if(!var8.hasNext()) {
                        return result;
                    }

                    Element e = (Element)var8.next();
                    String linkUrl = e.attr("ng-init");
                    matcher = Pattern.compile("\'\\d+\'").matcher(linkUrl);
                } while(!matcher.find());

                String productCategory = matcher.group();
                String url1 = "https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetJSONItems?data={\"WordList\":\"\",\"ItemNumber\":\"\",\"CategoryCode\":" + productCategory + ",\"SearchMethod\":\"Category\",\"Limit\":0}";
                String json = client.get(url1).body();
                Matcher matcher1 = Pattern.compile("ItemNumber\":\"(\\w+|\\d+)\"").matcher(json);
                StringBuilder sb = new StringBuilder();

                while(matcher1.find()) {
                    logger.info(matcher1.group(1));
                    sb.append(matcher1.group(1));
                    sb.append(',');
                }

                if(0 != sb.length()) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl("https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetItemsData?ItemNumbersString=" + sb.toString());
                    webPageEntity.setSourceBySourceId(webPageEntity.getSourceBySourceId());
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setType("productPage");
                    result.add(webPageEntity);
                }
            }
        } else {
            return result;
        }
    }

    public boolean canParse(String url, String action) {
        return url.startsWith("https://www.wolverinesupplies.com/") && action.equals("productList");
    }
}
