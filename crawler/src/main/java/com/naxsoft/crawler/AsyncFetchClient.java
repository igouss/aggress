package com.naxsoft.crawler;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Copyright NAXSoft 2015
 */
public class AsyncFetchClient<T> implements AutoCloseable, Cloneable {
    AsyncHttpClient asyncHttpClient;

    public AsyncFetchClient() {
        asyncHttpClient = new AsyncHttpClient();
    }

    public Future<T> get(String url, AsyncHandler<T> handler)  {
        return get(url, Collections.<Cookie> emptyList(), handler);
    }

    public Future<T> get(String url, Collection<Cookie> cookies, AsyncHandler<T> handler)  {

        AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient.prepareGet(url);
        requestBuilder.setRequestTimeout((int) TimeUnit.SECONDS.toMillis(60));
        requestBuilder.setCookies(cookies);
        requestBuilder.setFollowRedirects(true);
        return requestBuilder.execute(handler);
    }

    public Future<T> post(String url, String content, AsyncHandler<T> handler) throws ExecutionException, InterruptedException {
        return post(url, content, Collections.<Cookie>emptyList(), handler);
    }

    public ListenableFuture<T> post(String url, String content, Collection<Cookie> cookies, AsyncHandler<T> handler) {
        AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(url);
        requestBuilder.setRequestTimeout((int) TimeUnit.SECONDS.toMillis(60));
        requestBuilder.setCookies(cookies);
        requestBuilder.setBody(content);
        requestBuilder.setFollowRedirects(true);
        return requestBuilder.execute(handler);
    }

    public void close() {
        asyncHttpClient.close();
    }
}
