package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.CompletionHandler;
import com.ning.http.client.cookie.Cookie;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractWebPageParser implements WebPageParser {
    private final static CompletionHandler<List<Cookie>> cookieHandler = new CompletionHandler<List<Cookie>>() {
        @Override
        public List<Cookie> onCompleted(com.ning.http.client.Response resp) throws Exception {
            return resp.getCookies();
        }
    };

    protected static CompletionHandler<List<Cookie>> getCookiesHandler() {
        return cookieHandler;
    }
}
