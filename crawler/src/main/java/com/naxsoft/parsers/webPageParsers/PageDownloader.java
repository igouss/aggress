package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.ning.http.client.cookie.Cookie;
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
     * @param client HTTPClient
     * @param url    URL of pages to download
     * @return Stream of downloaded pages.
     */
    public static Future<WebPageEntity> download(HttpClient client, String url) {
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
    public static Future<WebPageEntity> download(HttpClient client, List<Cookie> cookies, String url) {
        return client.get(url, cookies, new AbstractCompletionHandler<WebPageEntity>() {
            @Override
            public WebPageEntity onCompleted(com.ning.http.client.Response response) throws Exception {
                WebPageEntity result = null;
                if (200 == response.getStatusCode()) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(url);
                    webPageEntity.setContent(response.getResponseBody());
                    webPageEntity.setParsed(false);
                    webPageEntity.setType("productPageRaw");
                    result = webPageEntity;
                    LOGGER.info("productPageRaw={}", webPageEntity.getUrl());
                }
                return result;
            }
        });
    }
}
