package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AbstractCompletionHandler;
import org.asynchttpclient.Response;
import org.asynchttpclient.cookie.Cookie;

import java.util.List;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractWebPageParser implements WebPageParser {
    /**
     * Return all the cookies contained in HTTP server response
     */
    private final static AbstractCompletionHandler<List<Cookie>> COOKIE_HANDLER = new AbstractCompletionHandler<List<Cookie>>() {
        @Override
        public List<Cookie> onCompleted(Response response) throws Exception {
            return response.getCookies();
        }
    };

    /**
     * @return HTTP cookie handler
     */
    protected static AbstractCompletionHandler<List<Cookie>> getCookiesHandler() {
        return COOKIE_HANDLER;
    }
}
