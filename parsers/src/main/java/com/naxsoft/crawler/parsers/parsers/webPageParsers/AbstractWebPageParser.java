package com.naxsoft.crawler.parsers.parsers.webPageParsers;

import com.naxsoft.http.AbstractCompletionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import okhttp3.Response;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractWebPageParser implements WebPageParser {
    /**
     * @return HTTP cookie handler
     */
    protected static AbstractCompletionHandler<List<Cookie>> getCookiesHandler() {
        /*Return all the cookies contained in HTTP server response*/
        return new AbstractCompletionHandler<List<Cookie>>() {
            @Override
            public List<Cookie> onCompleted(Response response) {
                log.info("Completed request to {}", response.request().url().toString());
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
