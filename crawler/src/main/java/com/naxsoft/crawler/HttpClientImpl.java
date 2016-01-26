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
 *
 * HTTP client. Can sent GET and POST requests
 */
public class HttpClientImpl implements AutoCloseable, Cloneable, HttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientImpl.class);

    private final static int MAX_CONNECTIONS = 3;
    public static final int REQUEST_TIMEOUT = (int) TimeUnit.SECONDS.toMillis(60);

    private final AsyncHttpClient asyncHttpClient;


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
                    LOGGER.error("ASyncHttpdClient error {} {}", ctx.getRequest().getUrl(), ctx.getIOException().getMessage());
                    return new FilterContext.FilterContextBuilder(ctx)
                            .request(ctx.getRequest())
                            .replayRequest(true)
                            .build();
                })
                .addRequestFilter(new ThrottleRequestFilter(MAX_CONNECTIONS))
                .build();
        asyncHttpClient = new AsyncHttpClient(asyncHttpClientConfig);
    }

    /**
     * Execute HTTP GET operation
     * @param url Page address
     * @param handler Completion handler
     * @param <R>
     * @return
     */
    @Override
    public <R> ListenableFuture<R> get(String url, AbstractCompletionHandler<R> handler) {
        return get(url, Collections.<Cookie>emptyList(), handler);
    }

    /**
     * Execute HTTP GET operation
     * @param url Page address
     * @param cookies Request cookies
     * @param handler Completion handler
     * @param <R>
     * @return
     */
    @Override
    public <R> ListenableFuture<R> get(String url, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler) {
        return get(url, cookies, handler, true);
    }

    /**
     * Execute HTTP GET operation
     * @param url Page address
     * @param cookies Request cookies
     * @param handler Completion handler
     * @param followRedirect Follow HTTP redirects
     * @param <R>
     * @return
     */
    @Override
    public <R> ListenableFuture<R> get(String url, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler, boolean followRedirect) {
        LOGGER.trace("Starting async http GET request url = {}", url);
        AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient.prepareGet(url);
        requestBuilder.setRequestTimeout(REQUEST_TIMEOUT);
        requestBuilder.setCookies(cookies);
        requestBuilder.setFollowRedirects(followRedirect);
        Request request = requestBuilder.build();
        return asyncHttpClient.executeRequest(request, handler);
    }

    /**
     * Execute HTTP POST operation
     * @param url Page address
     * @param content Content to send in a POST request
     * @param handler Completion handler
     * @param <R>
     * @return
     */
    @Override
    public <R> ListenableFuture<R> post(String url, String content, AbstractCompletionHandler<R> handler) {
        return post(url, content, Collections.<Cookie>emptyList(), handler);
    }

    /**
     * Execute HTTP POST operation
     * @param url Page address
     * @param content Content to send in a POST request
     * @param cookies Request cookies
     * @param handler Completion handler
     * @param <R>
     * @return
     */
    @Override
    public <R> ListenableFuture<R> post(String url, String content, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler) {
        LOGGER.trace("Starting async http POST request url = {}", url);
        AsyncHttpClient.BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(url);
        requestBuilder.setRequestTimeout(REQUEST_TIMEOUT);
        requestBuilder.setCookies(cookies);
        requestBuilder.setBody(content);
        requestBuilder.setFollowRedirects(true);
        Request request = requestBuilder.build();
        return asyncHttpClient.executeRequest(request, handler);
    }

    /**
     * Execute HTTP POST operation
     * @param url Page address
     * @param formParameters HTTP Form paramaters
     * @param cookies Request cookies
     * @param handler Completion handler
     * @param <R>
     * @return
     */
    @Override
    public <R> ListenableFuture<R> post(String url, Map<String, String> formParameters, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler) {
        LOGGER.trace("Starting async http POST request url = {}", url);
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

    /**
     * Close HTTP client
     */
    public void close() {
        asyncHttpClient.close();
    }
}
