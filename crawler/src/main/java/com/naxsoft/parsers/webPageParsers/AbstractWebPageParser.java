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
    private final static CompletionHandler<List<Cookie>> COOKIE_HANDLER = new CompletionHandler<List<Cookie>>() {
        @Override
        public List<Cookie> onCompleted(com.ning.http.client.Response response) throws Exception {
            return response.getCookies();
        }
    };

    /**
     *
     * @return
     */
    protected static CompletionHandler<List<Cookie>> getCookiesHandler() {
        return COOKIE_HANDLER;
    }
}
