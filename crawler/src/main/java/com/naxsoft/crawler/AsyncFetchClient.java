package com.naxsoft.crawler;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.client.filter.FilterContext;
import com.ning.http.client.filter.FilterException;
import com.ning.http.client.filter.IOExceptionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Copyright NAXSoft 2015
 */
public class AsyncFetchClient implements AutoCloseable, Cloneable {
    public static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(60);
    private static final Logger logger = LoggerFactory.getLogger(AsyncFetchClient.class);
    private final AsyncHttpClient asyncHttpClient;

    public AsyncFetchClient(SSLContext sslContext) {
        AsyncHttpClientConfig asyncHttpClientConfig = new AsyncHttpClientConfig.Builder()
                .setAcceptAnyCertificate(true)
                .setSSLContext(sslContext)
//                .setMaxConnections(10)
                .setMaxConnectionsPerHost(2)
                .setAcceptAnyCertificate(true)
                .addIOExceptionFilter(ctx -> {
                    logger.error("ASyncHttpdClient error", ctx.getIOException());
                    return ctx;
                })
                .build();
        asyncHttpClient = new AsyncHttpClient(asyncHttpClientConfig);
    }

    public <R> Future<R> get(String url, AsyncHandler<R> handler)  {
        return get(url, Collections.<Cookie> emptyList(), handler);
    }

    public <R> Future<R> get(String url, Collection<Cookie> cookies, AsyncHandler<R> handler)  {
        return get(url, cookies, handler, true);
    }

    public <R> Future<R> get(String url, Collection<Cookie> cookies, AsyncHandler<R> handler, boolean followRedirect)  {
        logger.trace("Starting async http GET request url = {}", url);
        AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient.prepareGet(url);
        requestBuilder.setRequestTimeout(REQUEST_TIMEOUT);
        requestBuilder.setCookies(cookies);
        requestBuilder.setFollowRedirects(followRedirect);

        return requestBuilder.execute(handler);
    }

    public <T> Future<T> post(String url, String content, AsyncHandler<T> handler) {
        return post(url, content, Collections.<Cookie>emptyList(), handler);
    }

    public <T> ListenableFuture<T> post(String url, String content, Collection<Cookie> cookies, AsyncHandler<T> handler) {
        logger.trace("Starting async http POST request url = {}", url);
        AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(url);
        requestBuilder.setRequestTimeout(REQUEST_TIMEOUT);
        requestBuilder.setCookies(cookies);
        requestBuilder.setBody(content);
        requestBuilder.setFollowRedirects(true);


        return requestBuilder.execute(handler);
    }
    public <T> ListenableFuture<T> post(String url, Map<String, String> formParameters, Collection<Cookie> cookies, AsyncHandler<T> handler) {
        logger.trace("Starting async http POST request url = {}", url);
        AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(url);
        requestBuilder.setRequestTimeout(REQUEST_TIMEOUT);
        requestBuilder.setCookies(cookies);
        requestBuilder.setFollowRedirects(true);

        Set<Map.Entry<String, String>> entries = formParameters.entrySet();
        for(Map.Entry<String, String> e : entries) {
            requestBuilder.addFormParam(e.getKey(), e.getValue());
        }

        return requestBuilder.execute(handler);
    }

    public void close() {
        asyncHttpClient.close();
    }
}
