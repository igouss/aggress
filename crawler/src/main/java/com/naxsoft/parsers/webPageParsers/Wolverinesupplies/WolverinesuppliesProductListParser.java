package com.naxsoft.parsers.webPageParsers.wolverinesupplies;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.ning.http.client.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WolverinesuppliesProductListParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(WolverinesuppliesProductListParser.class);
    private final HttpClient client;

    public WolverinesuppliesProductListParser(HttpClient client) {
        this.client = client;
    }

    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        Observable<WebPageEntity> tmp = Observable.create(subscriber -> {
            client.get(parent.getUrl(), new CompletionHandler<Void>() {
                @Override
                public Void onCompleted(Response resp) throws Exception {
                    if (200 == resp.getStatusCode()) {

                        Document document = Jsoup.parse(resp.getResponseBody(), parent.getUrl());
                        Elements elements = document.select("div[ng-init]");

                        for (Element e : elements) {
                            String linkUrl = e.attr("ng-init");
                            Matcher categoryMatcher = Pattern.compile("\'\\d+\'").matcher(linkUrl);

                            if (categoryMatcher.find()) {
                                String productCategory = categoryMatcher.group();
                                String productDetailsUrl = "https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetJSONItems?data={\"WordList\":\"\",\"ItemNumber\":\"\",\"CategoryCode\":" + productCategory + ",\"SearchMethod\":\"Category\",\"Limit\":0}";
                                WebPageEntity webPageEntity = new WebPageEntity();
                                webPageEntity.setType("tmp");
                                webPageEntity.setUrl(productDetailsUrl);
                                webPageEntity.setCategory(parent.getCategory());
                                subscriber.onNext(webPageEntity);
                            }
                        }
                    }
                    subscriber.onCompleted();
                    return null;
                }
            });
        });
        return tmp.flatMap(page -> getItemsData(page));
    }

    private Observable<WebPageEntity> getItemsData(WebPageEntity parent) {
        return Observable.create(subscriber -> {
            client.get(parent.getUrl(), new CompletionHandler<Void>() {
                @Override
                public Void onCompleted(com.ning.http.client.Response resp) throws Exception {

                    String productDetailsJson = resp.getResponseBody();
                    Matcher itemNumberMatcher = Pattern.compile("ItemNumber\":\"(\\w+|\\d+)\"").matcher(productDetailsJson);
                    StringBuilder sb = new StringBuilder();

                    while (itemNumberMatcher.find()) {
                        sb.append(itemNumberMatcher.group(1));
                        sb.append(',');
                    }

                    if (0 != sb.length()) {
                        WebPageEntity webPageEntity = new WebPageEntity();
                        webPageEntity.setUrl("https://www.wolverinesupplies.com/WebServices/ProductSearchService.asmx/GetItemsData?ItemNumbersString=" + sb);
                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                        webPageEntity.setType("productPage");
                        logger.info("productPage={}", webPageEntity.getUrl());
                        subscriber.onNext(webPageEntity);
                    }
                    subscriber.onCompleted();
                    return null;
                }
            });
        });
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.wolverinesupplies.com/") && webPage.getType().equals("productList");
    }
}
