package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import com.ning.http.client.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
public class PageDownloader {
    private static final Logger logger = LoggerFactory.getLogger(PageDownloader.class);


    /**
     * Download page from the url.
     *
     * @param client HTTPClient
     * @param url    URL of pages to download
     * @return Stream of downloaded pages.
     */
    public static Future<WebPageEntity> download(AsyncFetchClient client, String url) {
        return download(client, Collections.EMPTY_LIST, url);
    }

    /**
     * Download page from the url.
     *
     * @param url     URL of pages to download
     * @param client  HTTPClient
     * @param cookies HTML cookies
     * @return Stream of downloaded pages.
     */
    public static Future<WebPageEntity> download(AsyncFetchClient client, List<Cookie> cookies, String url) {
        return client.get(url, cookies, new CompletionHandler<WebPageEntity>() {
            @Override
            public WebPageEntity onCompleted(com.ning.http.client.Response resp) throws Exception {
                WebPageEntity result = null;
                if (200 == resp.getStatusCode()) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(url);
                    webPageEntity.setContent(resp.getResponseBody());
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setStatusCode(resp.getStatusCode());
                    webPageEntity.setType("productPageRaw");
                    result = webPageEntity;
                    logger.info("productPageRaw={}", webPageEntity.getUrl());
                }
                return result;
            }
        });
    }
}
