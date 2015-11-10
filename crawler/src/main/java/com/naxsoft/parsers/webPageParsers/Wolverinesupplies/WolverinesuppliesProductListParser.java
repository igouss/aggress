package com.naxsoft.parsers.webPageParsers.Wolverinesupplies;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.ning.http.client.AsyncCompletionHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WolverinesuppliesProductListParser implements WebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(WolverinesuppliesProductListParser.class);
    private AsyncFetchClient client;

    public WolverinesuppliesProductListParser(AsyncFetchClient client) {
        this.client = client;
    }

    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) throws Exception {
        Future<Set<WebPageEntity>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
                if (resp.getStatusCode() == 200) {

                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    Elements elements = document.select("div[ng-init]");

                    for (Element e : elements) {
                        String linkUrl = e.attr("ng-init");
                        Matcher categoryMatcher = Pattern.compile("\'\\d+\'").matcher(linkUrl);

                        if (categoryMatcher.find()) {
                            String productCategory = categoryMatcher.group();
                            String productDetailsUrl = "https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetJSONItems?data={\"WordList\":\"\",\"ItemNumber\":\"\",\"CategoryCode\":" + productCategory + ",\"SearchMethod\":\"Category\",\"Limit\":0}";
                            WebPageEntity webPageEntity = new WebPageEntity();
                            webPageEntity.setParent(webPage);
                            webPageEntity.setType("tmp");
                            webPageEntity.setUrl(productDetailsUrl);
                            result.add(webPageEntity);
                        }
                    }
                }
                return result;
            }
        });

        return Observable.from(future).flatMap(Observable::from).map(this::getItemsData).flatMap(Observable::from);
    }

    private Future<Set<WebPageEntity>> getItemsData(WebPageEntity parent) {
        return client.get(parent.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();

                String productDetailsJson = resp.getResponseBody();
                Matcher itemNumberMatcher = Pattern.compile("ItemNumber\":\"(\\w+|\\d+)\"").matcher(productDetailsJson);
                StringBuilder sb = new StringBuilder();

                while (itemNumberMatcher.find()) {
                    sb.append(itemNumberMatcher.group(1));
                    sb.append(',');
                }

                if (0 != sb.length()) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl("https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetItemsData?ItemNumbersString=" + sb.toString());
                    webPageEntity.setParent(parent.getParent().getParent());
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setType("productPage");
                    logger.info("productPage=" + webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
                return result;
            }
        });
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.wolverinesupplies.com/") && webPage.getType().equals("productList");
    }
}
