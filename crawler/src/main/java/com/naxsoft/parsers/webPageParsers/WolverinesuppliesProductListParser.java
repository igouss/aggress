package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.ning.http.client.AsyncCompletionHandler;
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
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WolverinesuppliesProductListParser implements WebPageParser {


    public Set<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        AsyncFetchClient<Set<WebPageEntity>> client = new AsyncFetchClient<>();

        Logger logger = LoggerFactory.getLogger(this.getClass());
        Future<Set<WebPageEntity>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
                if (resp.getStatusCode() == 200) {
                    Logger logger = LoggerFactory.getLogger(this.getClass());
                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    Elements elements = document.select("div[ng-init]");

                    for (Element e : elements) {
                        String linkUrl = e.attr("ng-init");
                        Matcher categoryMatcher = Pattern.compile("\'\\d+\'").matcher(linkUrl);

                        if (categoryMatcher.find()) {
                            String productCategory = categoryMatcher.group();
                            String productDetailsUrl = "https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetJSONItems?data={\"WordList\":\"\",\"ItemNumber\":\"\",\"CategoryCode\":" + productCategory + ",\"SearchMethod\":\"Category\",\"Limit\":0}";

                            AsyncFetchClient<Set<WebPageEntity>> client2 = new AsyncFetchClient<>();
                            Future<Set<WebPageEntity>> future2 = client2.get(productDetailsUrl, new AsyncCompletionHandler<Set<WebPageEntity>>() {
                                @Override
                                public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                                    HashSet<WebPageEntity> r = new HashSet<>();
                                    String productDetailsJson = resp.getResponseBody();
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
                                        logger.info("productPage=" + webPageEntity.getUrl() + ", parent=" + webPage.getUrl());
                                        r.add(webPageEntity);
                                    }
                                    return r;
                                }});
                            result.addAll(future2.get());
                            client2.close();
                        }
                    }
                }
                return result;
            }
        });
        try {
            Set<WebPageEntity> webPageEntities = future.get();
            client.close();
            return webPageEntities;
        } catch (Exception e) {
            logger.error("HTTP error", e);
            return new HashSet<>();
        }
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.wolverinesupplies.com/") && webPage.getType().equals("productList");
    }
}
