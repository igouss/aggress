package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import org.asynchttpclient.Response;
import org.asynchttpclient.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collections;
import java.util.List;

/**
 * Copyright NAXSoft 2015
 */
public class PageDownloader {
    /**
     * Download page from the url.
     *
     * @param client        HTTPClient
     * @param webPageEntity Page to download
     * @return Stream of downloaded pages.
     */
    public static Observable<WebPageEntity> download(HttpClient client, WebPageEntity webPageEntity, String type) {
        return download(client, Collections.emptyList(), webPageEntity, type);
    }

    /**
     * Download page from the url.
     *
     * @param client  HTTPClient
     * @param cookies HTML cookies
     * @param parent  Page to download
     * @return Stream of downloaded pages.
     */
    public static Observable<WebPageEntity> download(final HttpClient client, final List<Cookie> cookies, final WebPageEntity parent, final String type) {
        return client.get(parent.getUrl(), cookies, new AbstractCompletionHandler<WebPageEntity>() {
            private final Logger LOGGER = LoggerFactory.getLogger("com.naxsoft.parsers.webPageParsers.PageDownloader.Handler");
            private final String pageType = type;
            private final WebPageEntity parentPage = parent;

            @Override
            public WebPageEntity onCompleted(Response response) throws Exception {
                LOGGER.trace("Completed request to {} {}", parentPage.getType(), response.getUri().toString());
                WebPageEntity result = null;
                if (200 == response.getStatusCode()) {
                    result = new WebPageEntity(parentPage, response.getResponseBody(), pageType, false, response.getUri().toUrl(), parentPage.getCategory());
                } else {
                    LOGGER.error("Bad HTTP code={} page={}", response.getStatusCode(), response.getUri());
                }
                return result;
            }
        });
    }
}
