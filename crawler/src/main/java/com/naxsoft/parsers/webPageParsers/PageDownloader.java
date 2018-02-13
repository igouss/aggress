package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.Collections;
import java.util.List;
import okhttp3.Cookie;

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
//                LOGGER.info("Completed request to {} {}", parentPage.getType(), response.getUri().toString());
                WebPageEntity result = null;
                if (200 == response.code()) {
                    result = new WebPageEntity(parentPage, response.body().string(), pageType, response.request().url().toString(), parentPage.getCategory());
                } else {
                    LOGGER.error("Bad HTTP code={} page={}", response.code(), response.request().url().toString());
                }
                return result;
            }
        }).observeOn(Schedulers.io());
    }
}
