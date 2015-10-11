package com.naxsoft.parsers.webPageParsers.irunguns;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.ning.http.client.AsyncCompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
public class IrungunsProductPageParser implements WebPageParser {
    private AsyncFetchClient client;

    public IrungunsProductPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) throws Exception {
        Logger logger = LoggerFactory.getLogger(this.getClass());
        Future<Set<WebPageEntity>> future = client.get(webPage.getUrl(), new AsyncCompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(com.ning.http.client.Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
                if (resp.getStatusCode() == 200) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(webPage.getUrl());
                    webPageEntity.setParent(webPage);
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setType("productPageRaw");
                    webPageEntity.setContent(resp.getResponseBody());
                    webPageEntity.setParent(webPage);
                    result.add(webPageEntity);
                    logger.info("productPageRaw=" + webPageEntity.getUrl());
                }
                return result;
            }
        });
        // return Observable.defer(() -> Observable.just(future.get()));
        return Observable.defer(() -> Observable.from(future));
    }

    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("https://www.irunguns.us/") && webPage.getType().equals("productPage");
    }
}
