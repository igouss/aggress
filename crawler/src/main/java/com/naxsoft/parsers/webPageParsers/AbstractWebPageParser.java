package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.cookie.Cookie;

import java.util.List;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractWebPageParser implements WebPageParser {
    /**
     * Return all the cookies contained in HTTP server response
     */
    private final static AsyncCompletionHandler<List<Cookie>> COOKIE_HANDLER = new AbstractCompletionHandler<List<Cookie>>() {
        @Override
        public List<Cookie> onCompleted(com.ning.http.client.Response response) throws Exception {
            return response.getCookies();
        }
    };

    /**
     * @return HTTP cookie handler
     */
    protected static AsyncCompletionHandler<List<Cookie>> getCookiesHandler() {
        return COOKIE_HANDLER;
    }
}
