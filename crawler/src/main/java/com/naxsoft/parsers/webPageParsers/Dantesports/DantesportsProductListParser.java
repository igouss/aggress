package com.naxsoft.parsers.webPageParsers.Dantesports;

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

/**
 * Copyright NAXSoft 2015
 */
public class DantesportsProductListParser implements WebPageParser {
    private AsyncFetchClient client;

    public DantesportsProductListParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) throws Exception {
            Logger logger = LoggerFactory.getLogger(this.getClass());
            Future<Set<WebPageEntity>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
                @Override
                public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                    HashSet<WebPageEntity> result = new HashSet<>();
                    if (resp.getStatusCode() == 200) {
                        Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                        Elements elements = document.select("#store div.listItem");

                        for (Element element : elements) {
                            String onclick = element.attr("onclick");
                            Matcher matcher = Pattern.compile("\\d+").matcher(onclick);
                            if (matcher.find()) {
                                WebPageEntity webPageEntity = new WebPageEntity();
                                webPageEntity.setUrl("https://shop.dantesports.com/items_detail.php?iid=" + matcher.group());
                                webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                webPageEntity.setParsed(false);
                                webPageEntity.setStatusCode(resp.getStatusCode());
                                webPageEntity.setType("productPage");
                                webPageEntity.setParent(webPage);
                                logger.info("productPageUrl=" + webPageEntity.getUrl() + ", " + "parseUrl=" + webPage.getUrl());
                                result.add(webPageEntity);
                            } else {
                                logger.info("Product id not found: " + webPage);
                            }
                        }
                    }
                    return result;
                }
            });
        return Observable.defer(() -> Observable.from(future));

    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://shop.dantesports.com/") && webPage.getType().equals("productList");
    }
}
