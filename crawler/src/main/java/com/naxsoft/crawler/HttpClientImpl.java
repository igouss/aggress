package com.naxsoft.crawler;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Request;
import com.ning.http.client.cookie.Cookie;
import com.ning.http.client.extra.ThrottleRequestFilter;
import com.ning.http.client.filter.FilterContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Copyright NAXSoft 2015
 */
public class HttpClientImpl implements AutoCloseable, Cloneable, HttpClient {
    public static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(60);
    private static final Logger logger = LoggerFactory.getLogger(HttpClientImpl.class);
    private final AsyncHttpClient asyncHttpClient;
    private final int maxConnections = 1;

    public HttpClientImpl(SSLContext sslContext) {
        AsyncHttpClientConfig asyncHttpClientConfig = new AsyncHttpClientConfig.Builder()
                .setAcceptAnyCertificate(true)
                .setSSLContext(sslContext)
                .setAllowPoolingConnections(false)
                .setAllowPoolingSslConnections(false)
                .setMaxRequestRetry(10)
                .setPooledConnectionIdleTimeout((int) TimeUnit.MILLISECONDS.convert(60, TimeUnit.SECONDS))
                .setAcceptAnyCertificate(true)
                .addIOExceptionFilter(ctx -> {
                    logger.error("ASyncHttpdClient error {} {}", ctx.getRequest().getUrl(), ctx.getIOException().getMessage());
                    return new FilterContext.FilterContextBuilder(ctx)
                            .request(ctx.getRequest())
                            .replayRequest(true)
                            .build();
                })
                .addRequestFilter(new ThrottleRequestFilter(maxConnections))
                .build();
        asyncHttpClient = new AsyncHttpClient(asyncHttpClientConfig);
    }

    @Override
    public <R> ListenableFuture<R> get(String url, CompletionHandler<R> handler) {
        return get(url, Collections.<Cookie>emptyList(), handler);
    }

    @Override
    public <R> ListenableFuture<R> get(String url, Collection<Cookie> cookies, CompletionHandler<R> handler) {
        return get(url, cookies, handler, true);
    }

    @Override
    public <R> ListenableFuture<R> get(String url, Collection<Cookie> cookies, CompletionHandler<R> handler, boolean followRedirect) {
        logger.trace("Starting async http GET request url = {}", url);
        AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient.prepareGet(url);
        requestBuilder.setRequestTimeout(REQUEST_TIMEOUT);
        requestBuilder.setCookies(cookies);
        requestBuilder.setFollowRedirects(followRedirect);
        Request request = requestBuilder.build();
        return asyncHttpClient.executeRequest(request, handler);
    }

    @Override
    public <R> ListenableFuture<R> post(String url, String content, CompletionHandler<R> handler) {
        return post(url, content, Collections.<Cookie>emptyList(), handler);
    }

    @Override
    public <R> ListenableFuture<R> post(String url, String content, Collection<Cookie> cookies, CompletionHandler<R> handler) {
        logger.trace("Starting async http POST request url = {}", url);
        AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(url);
        requestBuilder.setRequestTimeout(REQUEST_TIMEOUT);
        requestBuilder.setCookies(cookies);
        requestBuilder.setBody(content);
        requestBuilder.setFollowRedirects(true);
        Request request = requestBuilder.build();
        return asyncHttpClient.executeRequest(request, handler);
    }

    @Override
    public <R> ListenableFuture<R> post(String url, Map<String, String> formParameters, Collection<Cookie> cookies, CompletionHandler<R> handler) {
        logger.trace("Starting async http POST request url = {}", url);
        AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(url);
        requestBuilder.setRequestTimeout(REQUEST_TIMEOUT);
        requestBuilder.setCookies(cookies);
        requestBuilder.setFollowRedirects(true);
        Request request = requestBuilder.build();

        Set<Map.Entry<String, String>> entries = formParameters.entrySet();
        for (Map.Entry<String, String> e : entries) {
            requestBuilder.addFormParam(e.getKey(), e.getValue());
        }

        return asyncHttpClient.executeRequest(request, handler);
    }

    public void close() {
        asyncHttpClient.close();
    }
}
