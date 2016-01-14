package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import com.ning.http.client.cookie.Cookie;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Copyright NAXSoft 2015
 */
public class PageDownloader {
    private static final Logger logger = LoggerFactory.getLogger(PageDownloader.class);


    /**
     * Download page from the url.
     * @param pages URLs of pages to download
     * @param client HTTPClient
     * @return Stream of downloaded pages.
     */
    public static Observable<WebPageEntity> download(AsyncFetchClient client, String...pages) {
        return download(client, Collections.EMPTY_LIST, pages);
    }

    /**
     * Download page from the url.
     * @param pages URLs of pages to download
     * @param client HTTPClient
     * @param cookies HTML cookies
     * @return Stream of downloaded pages.
     */
    public static Observable<WebPageEntity> download(AsyncFetchClient client, List<Cookie> cookies, String...pages) {
        return Observable.create(subscriber -> {
            for(String url : pages) {
                client.get(url, cookies, new CompletionHandler<Void>() {
                    @Override
                    public Void onCompleted(com.ning.http.client.Response resp) throws Exception {
                        if (200 == resp.getStatusCode()) {
                            WebPageEntity webPageEntity = new WebPageEntity();
                            webPageEntity.setUrl(url);
                            webPageEntity.setContent(resp.getResponseBody());
                            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                            webPageEntity.setParsed(false);
                            webPageEntity.setStatusCode(resp.getStatusCode());
                            webPageEntity.setType("productPageRaw");
                            subscriber.onNext(webPageEntity);
                            logger.info("productPageRaw={}", webPageEntity.getUrl());
                        }
                        subscriber.onCompleted();
                        return null;
                    }
                });
            }
        });
    }


}
