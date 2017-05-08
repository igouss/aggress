package com.naxsoft.crawler;

import okhttp3.*;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Copyright NAXSoft 2015
 * <p>
 * HTTP client. Can sent GET and POST requests
 */
public class AhcHttpClient implements HttpClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(AhcHttpClient.class);

    private final static int MAX_CONNECTIONS = 5;

    private final Meter httpRequestsSensor;
    private final Histogram httpResponseSizeSensor;
    private final Histogram httpLatencySensor;
    private final OkHttpClient client;
    private final CookieManager cookieManager;
    public AhcHttpClient(MetricRegistry metricRegistry, SslContext sslContext) {
        httpRequestsSensor = metricRegistry.meter("http.requests");
        httpResponseSizeSensor = metricRegistry.histogram("http.responseSize");
        httpLatencySensor = metricRegistry.histogram("http.latency");

        // TODO: http://stackoverflow.com/questions/25461792/persistent-cookie-store-using-okhttp-2-on-android/25462286#25462286
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        client = new OkHttpClient.Builder()
                .cookieJar(new JavaNetCookieJar(cookieManager))
                .build();
    }

    /**
     * Execute HTTP GET operation
     *
     * @param url     Page address
     * @param handler Completion handler
     * @param <R>     Type of the value that will be returned by the associated Future
     * @return a Future of type T
     */

    @Override
    public <R> Observable<R> get(String url, AbstractCompletionHandler<R> handler) {
        return get(url, Collections.emptyList(), handler);
    }

    /**
     * Execute HTTP GET operation
     *
     * @param url     Page address
     * @param cookies Request cookies
     * @param handler Completion handler
     * @param <R>     Type of the value that will be returned by the associated Future
     * @return a Future of type T
     */
    @Override
    public <R> Observable<R> get(String url, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler) {
        return get(url, cookies, handler, true);
    }

    /**
     * Execute HTTP GET operation
     *
     * @param <R>            Type of the value that will be returned by the associated Future
     * @param url            Page address
     * @param cookies        Request cookies
     * @param handler        Completion handler
     * @param followRedirect Follow HTTP redirects
     * @return a Future of type T
     */
    @Override
    public <R> Observable<R> get(String url, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler, boolean followRedirect) {
        LOGGER.trace("Starting async http GET request url = {}", url);
        httpRequestsSensor.mark();

        for (Cookie cookie : cookies) {
            cookieManager.getCookieStore().add(URI.create(url), new HttpCookie(cookie.name(), cookie.value()));
        }

        Request request = new Request.Builder()
                .url(url)
                .build();
        return Observable.create(subscriber -> {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    subscriber.onError(e);
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        subscriber.onNext(handler.onCompleted(response));
                        subscriber.onCompleted();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }
            });
        }, Emitter.BackpressureMode.BUFFER);
    }


    /**
     * Execute HTTP POST operation
     *
     * @param url     Page address
     * @param content Content to send in a POST request
     * @param handler Completion handler
     * @param <R>     Type of the value that will be returned by the associated Future
     * @return a Future of type T
     */
    @Override
    public <R> Observable<R> post(String url, String content, AbstractCompletionHandler<R> handler) {
        LOGGER.debug("Starting async http POST request url = {}", url);
        httpRequestsSensor.mark();

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("text/html; charset=utf-8"), content))
                .build();
        return Observable.create(subscriber -> {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    subscriber.onError(e);
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        subscriber.onNext(handler.onCompleted(response));
                        subscriber.onCompleted();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }
            });
        }, Emitter.BackpressureMode.BUFFER);
    }

    /**
     * Execute HTTP POST operation
     *
     * @param <R>            Type of the value that will be returned by the associated Future
     * @param url            Page address
     * @param formParameters HTTP Form parameters
     * @param cookies        Request cookies
     * @param handler        Completion handler
     * @return a Future of type T
     */
    @Override
    public <R> Observable<R> post(String url, Map<String, String> formParameters, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler) {
        LOGGER.debug("Starting async http POST request url = {}", url);
        httpRequestsSensor.mark();
        for (Cookie cookie : cookies) {
            cookieManager.getCookieStore().add(URI.create(url), new HttpCookie(cookie.name(), cookie.value()));
        }

        FormBody.Builder builder = new FormBody.Builder();
        for (String para : formParameters.keySet()) {
            builder.add(para, formParameters.get(para));
        }
        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();
        return Observable.create(subscriber -> {
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    subscriber.onError(e);
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        subscriber.onNext(handler.onCompleted(response));
                        subscriber.onCompleted();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }
            });
        }, Emitter.BackpressureMode.BUFFER);
    }

    /**
     * Close HTTP client
     */
    public void close() throws java.io.IOException {
    }
}
