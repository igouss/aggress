package com.naxsoft.parsers.webPageParsers.firearmsoutletcanada;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.naxsoft.utils.AppProperties;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
public class FirearmsoutletcanadaProductPageParser implements WebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(FirearmsoutletcanadaProductPageParser.class);
    private final AsyncFetchClient client;

    public FirearmsoutletcanadaProductPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) throws Exception {
        Future<Set<WebPageEntity>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
                if (200 == resp.getStatusCode()) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(webPage.getUrl());
                    webPageEntity.setContent(resp.getResponseBody());
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setStatusCode(resp.getStatusCode());
                    webPageEntity.setType("productPageRaw");
                    webPageEntity.setParent(webPage);
                    result.add(webPageEntity);
                    logger.info("productPageRaw={}", webPageEntity.getUrl());
                }
                return result;
            }
        });
        return Observable.from(future);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.firearmsoutletcanada.com/") && webPage.getType().equals("productPage");
    }
}