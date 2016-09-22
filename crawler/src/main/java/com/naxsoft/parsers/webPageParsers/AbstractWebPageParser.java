package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AbstractCompletionHandler;
import io.vertx.core.AbstractVerticle;
import org.asynchttpclient.Response;
import org.asynchttpclient.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractWebPageParser extends AbstractVerticle implements WebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebPageParser.class);

    /**
     * Return all the cookies contained in HTTP server response
     */
    private final static AbstractCompletionHandler<List<Cookie>> COOKIE_HANDLER = new AbstractCompletionHandler<List<Cookie>>() {
        private final Logger LOGGER = LoggerFactory.getLogger("com.naxsoft.parsers.webPageParsers.CookieCompletionHandler");

        @Override
        public List<Cookie> onCompleted(Response response) throws Exception {
            LOGGER.info("Completed request to {}", response.getUri().toString());
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
