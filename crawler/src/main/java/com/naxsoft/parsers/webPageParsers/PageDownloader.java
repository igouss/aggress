package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import org.asynchttpclient.Response;
import org.asynchttpclient.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
public class PageDownloader {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageDownloader.class);


    /**
     * Download page from the url.
     *
     * @param client        HTTPClient
     * @param webPageEntity Page to download
     * @return Stream of downloaded pages.
     */
    public static Future<WebPageEntity> download(HttpClient client, WebPageEntity webPageEntity) {
        return download(client, Collections.emptyList(), webPageEntity);
    }

    /**
     * Download page from the url.
     *
     * @param parent  Page to download
     * @param client  HTTPClient
     * @param cookies HTML cookies
     * @return Stream of downloaded pages.
     */
    public static Future<WebPageEntity> download(HttpClient client, List<Cookie> cookies, WebPageEntity parent) {
        return client.get(parent.getUrl(), cookies, new AbstractCompletionHandler<WebPageEntity>() {
            @Override
            public WebPageEntity onCompleted(Response response) throws Exception {
                WebPageEntity result = null;
                if (200 == response.getStatusCode()) {
                    WebPageEntity webPageEntity = new WebPageEntity(0L, response.getResponseBody(), "productPageRaw", false, response.getUri().toUrl(), parent.getCategory());
                    result = webPageEntity;
                    LOGGER.info("productPageRaw={}", webPageEntity.getUrl());
                } else {
                    LOGGER.error("Bad HTTP code={} page={}", response.getStatusCode(), response.getUri());
                }
                return result;
            }
        });
    }
}
