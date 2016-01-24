package com.naxsoft.parsers.webPageParsers.alflahertys;

import com.naxsoft.AbstractTest;
import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.cookie.Cookie;
import org.junit.Test;

import java.util.Collection;
import java.util.Map;

/**
 * Copyright NAXSoft 2015
 */
public class AlflahertysFrontPageParserTest extends AbstractTest {
    @Test
    public void parse() {

        AlflahertysFrontPageParser parser = new AlflahertysFrontPageParser(new HttpClient() {
            @Override
            public <R> ListenableFuture<R> get(String url, CompletionHandler<R> handler) {
                return null;
            }

            @Override
            public <R> ListenableFuture<R> get(String url, Collection<Cookie> cookies, CompletionHandler<R> handler) {
                return null;
            }

            @Override
            public <R> ListenableFuture<R> get(String url, Collection<Cookie> cookies, CompletionHandler<R> handler, boolean followRedirect) {
                return null;
            }

            @Override
            public <R> ListenableFuture<R> post(String url, String content, CompletionHandler<R> handler) {
                return null;
            }

            @Override
            public <R> ListenableFuture<R> post(String url, String content, Collection<Cookie> cookies, CompletionHandler<R> handler) {
                return null;
            }

            @Override
            public <R> ListenableFuture<R> post(String url, Map<String, String> formParameters, Collection<Cookie> cookies, CompletionHandler<R> handler) {
                return null;
            }
        });
        parser.parse(new WebPageEntity());
    }
}