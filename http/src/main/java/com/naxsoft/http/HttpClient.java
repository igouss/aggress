package com.naxsoft.http;


import okhttp3.Cookie;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HttpClient interface, in case we need to replace actual implementation for mocking purposes.
 */
public interface HttpClient extends AutoCloseable {
    /**
     * Perform an HTTP GET request
     *
     * @param url Page address
     */
    <R> CompletableFuture<R> get(String url, AbstractCompletionHandler<R> handler);

    /**
     * Perform an HTTP GET request
     *
     * @param url     Page address
     * @param cookies Request cookies
     * @return a Future of type R
     */
    <R> CompletableFuture<R> get(String url, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler);

    /**
     * Perform an HTTP GET request
     *
     * @param url            Page address
     * @param cookies        Request cookies
     * @param followRedirect Follow HTTP redirects
     */
    <R> CompletableFuture<R> get(String url, Collection<Cookie> cookies, boolean followRedirect, AbstractCompletionHandler<R> handler);

    /**
     * Perform an HTTP POST request
     *
     * @param url     Page address
     * @param content Content to send in a POST request
     * @return a Future of type R
     */
    <R> CompletableFuture<R> post(String url, String content, AbstractCompletionHandler<R> handler);

    /**
     * Perform an HTTP POST request
     *
     * @param url            Page address
     * @param formParameters HTTP Form paramaters
     * @param cookies        Request cookies
     */
    <R> CompletableFuture<R> post(String url, Map<String, String> formParameters, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler);

    /**
     * Close HTTP client
     */
    void close() throws java.io.IOException;
}
