package com.naxsoft.crawler;

import com.ning.http.client.ListenableFuture;
import com.ning.http.client.cookie.Cookie;

import java.util.Collection;
import java.util.Map;

/**
 * Copyright NAXSoft 2015
 *
 * HttpClient interface, in case we need to replace actual implementation for mocking purposes.
 */
public interface HttpClient extends AutoCloseable {
    /**
     * Perform an HTTP GET request
     * @param url Page address
     * @param handler Completion handler
     * @param <R> resource type returned by Completion handler
     * @return
     */
    <R> ListenableFuture<R> get(String url, AbstractCompletionHandler<R> handler);

    /**
     * Perform an HTTP GET request
     * @param url Page address
     * @param cookies Request cookies
     * @param handler Completion handler
     * @param <R> result of an asynchronous computation.
     * @return
     */
    <R> ListenableFuture<R> get(String url, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler);

    /**
     * Perform an HTTP GET request
     * @param url Page address
     * @param cookies Request cookies
     * @param handler Completion handler
     * @param followRedirect Follow HTTP redirects
     * @param <R> result of an asynchronous computation.
     * @return
     */
    <R> ListenableFuture<R> get(String url, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler, boolean followRedirect);

    /**
     * Perform an HTTP POST request
     * @param url Page address
     * @param content Content to send in a POST request
     * @param handler Completion handler
     * @param <R> result of an asynchronous computation.
     * @return
     */
    <R> ListenableFuture<R> post(String url, String content, AbstractCompletionHandler<R> handler);

    /**
     * Perform an HTTP POST request
     * @param url Page address
     * @param content Content to send in a POST request
     * @param cookies Request cookies
     * @param handler Completion handler
     * @param <R> result of an asynchronous computation.
     * @return
     */
    <R> ListenableFuture<R> post(String url, String content, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler);

    /**
     * Perform an HTTP POST request
     * @param url Page address
     * @param formParameters HTTP Form paramaters
     * @param cookies Request cookies
     * @param handler Completion handler
     * @param <R> result of an asynchronous computation.
     * @return
     */
    <R> ListenableFuture<R> post(String url, Map<String, String> formParameters, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler);
}
