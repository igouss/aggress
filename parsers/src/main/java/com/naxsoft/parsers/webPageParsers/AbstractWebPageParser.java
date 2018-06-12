package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.http.AbstractCompletionHandler;
import com.naxsoft.http.HttpClient;
import okhttp3.Cookie;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public abstract class AbstractWebPageParser implements WebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebPageParser.class);
    protected final HttpClient client;

    public AbstractWebPageParser(HttpClient client) {
        this.client = client;
    }

    /**
     * @return HTTP cookie handler
     */
    protected static AbstractCompletionHandler<List<Cookie>> getCookiesHandler() {
        /*Return all the cookies contained in HTTP server response*/
        return new AbstractCompletionHandler<List<Cookie>>() {
            private final Logger LOGGER = LoggerFactory.getLogger("com.naxsoft.parsers.webPageParsers.CookieCompletionHandler");

            @Override
            public List<Cookie> onCompleted(Response response) {
                LOGGER.info("Completed request to {}", response.request().url().toString());
                return Collections.emptyList();
                // return response.getCookies();
            }
        };
    }

    /**
     * @return website this parser can parse
     */
    protected abstract String getSite();

    /**
     * @return type of the page this parser can parse
     */
    protected abstract String getParserType();
}
