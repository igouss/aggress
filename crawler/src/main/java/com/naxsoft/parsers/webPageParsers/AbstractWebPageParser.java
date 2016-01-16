package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.CompletionHandler;
import com.ning.http.client.cookie.Cookie;

import java.util.List;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractWebPageParser implements WebPageParser {
    /**
     *
     */
    private final static CompletionHandler<List<Cookie>> cookieHandler = new CompletionHandler<List<Cookie>>() {
        @Override
        public List<Cookie> onCompleted(com.ning.http.client.Response resp) throws Exception {
            return resp.getCookies();
        }
    };

    /**
     *
     * @return
     */
    protected static CompletionHandler<List<Cookie>> getCookiesHandler() {
        return cookieHandler;
    }
}
