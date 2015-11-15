package com.naxsoft.parsers.webPageParsers.dantesports;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.cookie.Cookie;
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
public class DantesportsProductPageParser implements WebPageParser {
    private AsyncFetchClient client;
    private static final Logger logger = LoggerFactory.getLogger(DantesportsProductPageParser.class);

    public DantesportsProductPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) throws Exception {
        List<Cookie> cookies = new LinkedList<>();

        Future<List<Cookie>> future = client.get("https://shop.dantesports.com/set_lang.php?lang=EN", cookies, getEngCookiesHandler(), false);
        cookies.addAll(future.get());


        Future<Set<WebPageEntity>> future2 = client.get(webPage.getUrl(), cookies, new AsyncCompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
                if (resp.getStatusCode() == 200) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(webPage.getUrl());
                    webPageEntity.setContent(resp.getResponseBody());
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setStatusCode(resp.getStatusCode());
                    webPageEntity.setType("productPageRaw");
                    webPageEntity.setParent(webPage);
                    result.add(webPageEntity);
                    logger.info("productPageRaw=" + webPageEntity.getUrl());
                }
                return result;
            }
        });
        return Observable.defer(() -> Observable.from(future2));
    }

    private AsyncCompletionHandler<List<Cookie>> getEngCookiesHandler() {
        return new AsyncCompletionHandler<List<Cookie>>() {
            @Override
            public List<Cookie> onCompleted(com.ning.http.client.Response resp) throws Exception {
                List<Cookie> cookies = resp.getCookies();
                return cookies;

            }
        };
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://shop.dantesports.com/") && webPage.getType().equals("productPage");
    }
}
