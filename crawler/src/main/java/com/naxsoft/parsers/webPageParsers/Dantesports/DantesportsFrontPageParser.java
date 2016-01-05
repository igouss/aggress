package com.naxsoft.parsers.webPageParsers.dantesports;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
public class DantesportsFrontPageParser extends AbstractWebPageParser {
    private final AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(DantesportsFrontPageParser.class);

    public DantesportsFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) throws Exception {
        List<Cookie> cookies = new LinkedList<>();

        Future<List<Cookie>> future = client.get("https://shop.dantesports.com/set_lang.php?lang=EN", cookies, getEngCookiesHandler(), false);
        cookies.addAll(future.get());


        Future<Set<WebPageEntity>> future2 = client.get(webPage.getUrl(), cookies, new CompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
                if (200 == resp.getStatusCode()) {
                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    Elements elements = document.select("#scol1 > div.scell_menu > li > a");

                    for (Element element : elements) {
                        WebPageEntity webPageEntity = new WebPageEntity();
                        webPageEntity.setUrl(element.attr("abs:href") + "&paging=0");
                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                        webPageEntity.setParsed(false);
                        webPageEntity.setStatusCode(resp.getStatusCode());
                        webPageEntity.setType("productList");
                        logger.info("productList={}, parent={}", webPageEntity.getUrl(), webPage.getUrl());
                        result.add(webPageEntity);
                    }

                }
                return result;
            }
        });
        return Observable.from(future2);
    }

    private static CompletionHandler<List<Cookie>> getEngCookiesHandler() {
        return new CompletionHandler<List<Cookie>>() {
            @Override
            public List<Cookie> onCompleted(com.ning.http.client.Response resp) throws Exception {
                return resp.getCookies();

            }
        };
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://shop.dantesports.com/") && webPage.getType().equals("frontPage");
    }
}
