package com.naxsoft.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * HTTP client. Can sent GET and POST requests
 */
@Slf4j
public class AhcHttpClient implements HttpClient {

//    private final static int MAX_CONNECTIONS = 5;

    //    private final Histogram httpResponseSizeSensor;
//    private final Histogram httpLatencySensor;
    private final OkHttpClient client;
    private final CookieManager cookieManager;

//    private Map<String, Bucket> domainRateLimiter = new HashMap<>();

    public AhcHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
//        httpResponseSizeSensor = metricRegistry.histogram("http.responseSize");
//        httpLatencySensor = metricRegistry.histogram("http.latency");


        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {

                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Install all-trusting host name verifier
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, sslSession) -> true);


        // TODO: http://stackoverflow.com/questions/25461792/persistent-cookie-store-using-okhttp-2-on-android/25462286#25462286
        cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        client = new OkHttpClient.Builder()
                .sslSocketFactory(sc.getSocketFactory(), (X509TrustManager) trustAllCerts[0])
                .hostnameVerifier((s, sslSession) -> true)
                .build();
        client.dispatcher().setMaxRequestsPerHost(1);
    }

    /**
     * Execute HTTP GET operation
     *
     * @param url Page address
     * @return a Future of type T
     */

    @Override
    public <R> CompletableFuture<R> get(String url, AbstractCompletionHandler<R> handler) {
        return get(url, Collections.emptyList(), handler);
    }

    /**
     * Execute HTTP GET operation
     *
     * @param url     Page address
     * @param cookies Request cookies
     * @return a Future of type T
     */
    @Override
    public <R> CompletableFuture<R> get(String url, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler) {
        return get(url, cookies, true, handler);
    }

    /**
     * Execute HTTP GET operation
     *
     * @param url            Page address
     * @param cookies        Request cookies
     * @param followRedirect Follow HTTP redirects
     * @return a Future of type T
     */
    @Override
    public <R> CompletableFuture<R> get(String url, Collection<Cookie> cookies, boolean followRedirect, AbstractCompletionHandler<R> handler) {
        log.trace("Starting async http GET request url = {}", url);

        URI uri = URI.create(url);

//        String host = uri.getHost();
//        if (!domainRateLimiter.containsKey(host)) {
//            // define the limit 100 times per 1 minute
//            Bandwidth limit = Bandwidth.simple(100, Duration.ofMinutes(1));
//            // construct the bucket
//            Bucket bucket = Bucket4j.builder().addLimit(limit).build();
//            domainRateLimiter.put(host, bucket);
//        }

//            domainRateLimiter.get(host).consume(1, BlockingStrategy.PARKING);
        for (Cookie cookie : cookies) {
            cookieManager.getCookieStore().add(uri, new HttpCookie(cookie.name(), cookie.value()));
        }
        CompletableFuture<R> future = new CompletableFuture<>();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    future.complete(handler.onCompleted(response));
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }
        });
        return future;
    }


    /**
     * Execute HTTP POST operation
     *
     * @param url     Page address
     * @param content Content to send in a POST request
     * @return a Future of type T
     */
    @Override
    public <R> CompletableFuture<R> post(String url, String content, AbstractCompletionHandler<R> handler) {
        log.debug("Starting async http POST request url = {}", url);

//        URI uri = URI.create(url);
//        String host = uri.getHost();
//        if (!domainRateLimiter.containsKey(host)) {
//            // define the limit 100 times per 1 minute
//            Bandwidth limit = Bandwidth.simple(100, Duration.ofMinutes(1));
//            // construct the bucket
//            Bucket bucket = Bucket4j.builder().addLimit(limit).build();
//            domainRateLimiter.put(host, bucket);
//        }
//            domainRateLimiter.get(host).consume(1, BlockingStrategy.PARKING);

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("text/html; charset=utf-8"), content))
                .build();
        CompletableFuture<R> future = new CompletableFuture<>();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    future.complete(handler.onCompleted(response));
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }
        });
        return future;

    }

    /**
     * Execute HTTP POST operation
     *
     * @param url            Page address
     * @param formParameters HTTP Form parameters
     * @param cookies        Request cookies
     * @return a Future of type T
     */
    @Override
    public <R> CompletableFuture<R> post(String url, Map<String, String> formParameters, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler) {
        log.debug("Starting async http POST request url = {}", url);


//        URI uri = URI.create(url);
//        String host = uri.getHost();
//        if (!domainRateLimiter.containsKey(host)) {
//            // define the limit 100 times per 1 minute
//            Bandwidth limit = Bandwidth.simple(100, Duration.ofMinutes(1));
//            // construct the bucket
//            Bucket bucket = Bucket4j.builder().addLimit(limit).build();
//            domainRateLimiter.put(host, bucket);
//        }
//            domainRateLimiter.get(host).consume(1, BlockingStrategy.PARKING);

        for (Cookie cookie : cookies) {
            cookieManager.getCookieStore().add(URI.create(url), new HttpCookie(cookie.name(), cookie.value()));
        }

        FormBody.Builder builder = new FormBody.Builder();
        for (String para : formParameters.keySet()) {
            builder.add(para, formParameters.get(para));
        }
        CompletableFuture<R> future = new CompletableFuture<>();

        Request request = new Request.Builder()
                .url(url)
                .post(builder.build())
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try {
                    future.complete(handler.onCompleted(response));
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }
        });
        return future;
    }

    /**
     * Close HTTP client
     */
    public void close() {
    }
}
