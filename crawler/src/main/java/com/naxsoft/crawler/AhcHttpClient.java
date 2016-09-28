package com.naxsoft.crawler;

import io.netty.handler.ssl.SslContext;
import org.asynchttpclient.*;
import org.asynchttpclient.cookie.Cookie;
import org.asynchttpclient.filter.ThrottleRequestFilter;
import org.asynchttpclient.handler.resumable.ResumableIOExceptionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

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

    private final AsyncHttpClient asyncHttpClient;

    private final ProxyManager proxyManager;

    public AhcHttpClient(SslContext sslContext) {
        proxyManager = new ProxyManager();

        String osName = System.getProperty("os.name").toLowerCase();

        /*
        Overwrite defaults:
            org.asynchttpclient.threadPoolName=AsyncHttpClient
            org.asynchttpclient.maxConnections=-1
            org.asynchttpclient.maxConnectionsPerHost=-1
            org.asynchttpclient.connectTimeout=5000
            org.asynchttpclient.pooledConnectionIdleTimeout=60000
            org.asynchttpclient.connectionPoolCleanerPeriod=1000
            org.asynchttpclient.readTimeout=60000
            org.asynchttpclient.requestTimeout=60000
            org.asynchttpclient.connectionTtl=-1
            org.asynchttpclient.followRedirect=false
            org.asynchttpclient.maxRedirects=5
            org.asynchttpclient.compressionEnforced=false
            org.asynchttpclient.userAgent=AHC/2.0
            org.asynchttpclient.enabledProtocols=TLSv1.2, TLSv1.1, TLSv1
            org.asynchttpclient.useProxySelector=false
            org.asynchttpclient.useProxyProperties=false
            org.asynchttpclient.validateResponseHeaders=true
            org.asynchttpclient.strict302Handling=false
            org.asynchttpclient.keepAlive=true
            org.asynchttpclient.maxRequestRetry=5
            org.asynchttpclient.disableUrlEncodingForBoundRequests=false
            org.asynchttpclient.removeQueryParamOnRedirect=true
            org.asynchttpclient.useOpenSsl=false
            org.asynchttpclient.acceptAnyCertificate=false
            org.asynchttpclient.sslSessionCacheSize=0
            org.asynchttpclient.sslSessionTimeout=0
            org.asynchttpclient.tcpNoDelay=true
            org.asynchttpclient.soReuseAddress=false
            org.asynchttpclient.soLinger=-1
            org.asynchttpclient.soSndBuf=-1
            org.asynchttpclient.soRcvBuf=-1
            org.asynchttpclient.httpClientCodecMaxInitialLineLength=4096
            org.asynchttpclient.httpClientCodecMaxHeaderSize=8192
            org.asynchttpclient.httpClientCodecMaxChunkSize=8192
            org.asynchttpclient.disableZeroCopy=false
            org.asynchttpclient.handshakeTimeout=10000
            org.asynchttpclient.chunkedFileChunkSize=8192
            org.asynchttpclient.webSocketMaxBufferSize=128000000
            org.asynchttpclient.webSocketMaxFrameSize=10240
            org.asynchttpclient.keepEncodingHeader=false
            org.asynchttpclient.shutdownQuietPeriod=2000
            org.asynchttpclient.shutdownTimeout=15000
            org.asynchttpclient.useNativeTransport=false
            org.asynchttpclient.usePooledMemory=true
        */

        // Since 4.0.16, Netty provides the native socket transport for Linux using JNI.
        // This transport has higher performance and produces less garbage
        boolean useNativeTransport = osName.contains("linux"); //


        AsyncHttpClientConfig asyncHttpClientConfig = new DefaultAsyncHttpClientConfig.Builder()
                .setAcceptAnyCertificate(true)
                .setSslContext(sslContext)
                .setHandshakeTimeout((int) TimeUnit.SECONDS.toMillis(5))
                .setMaxRequestRetry(10)
                .setAcceptAnyCertificate(true)
                .addIOExceptionFilter(new ResumableIOExceptionFilter())
                .addRequestFilter(new ThrottleRequestFilter(MAX_CONNECTIONS))
                .setUseNativeTransport(useNativeTransport)
                .setUsePooledMemory(true)
                .build();
        asyncHttpClient = new DefaultAsyncHttpClient(asyncHttpClientConfig);
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
        BoundRequestBuilder requestBuilder = asyncHttpClient.prepareGet(url);
        requestBuilder.setCookies(cookies);
        requestBuilder.setFollowRedirect(followRedirect);
        requestBuilder.setProxyServer(proxyManager.getProxyServer());
        Request request = requestBuilder.build();

        handler.setProxyManager(proxyManager);
        int requestSize = request.getByteData().length;

        return Observable.from(asyncHttpClient.executeRequest(request, new StatsRecodringCompletionHandlerWrapper(requestSize, handler)));
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
        return post(url, content, Collections.emptyList(), handler);
    }

    /**
     * Execute HTTP POST operation
     *
     * @param <R>     Type of the value that will be returned by the associated Future
     * @param url     Page address
     * @param content Content to send in a POST request
     * @param cookies Request cookies
     * @param handler Completion handler
     * @return a Future of type T
     */
    @Override
    public <R> Observable<R> post(String url, String content, Collection<Cookie> cookies, AbstractCompletionHandler<R> handler) {
        LOGGER.debug("Starting async http POST request url = {}", url);
        BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(url);
        requestBuilder.setCookies(cookies);
        requestBuilder.setBody(content);
        requestBuilder.setFollowRedirect(true);
        requestBuilder.setRequestTimeout((int) TimeUnit.MINUTES.toMillis(2L));
        requestBuilder.setProxyServer(proxyManager.getProxyServer());

        Request request = requestBuilder.build();

        handler.setProxyManager(proxyManager);

        return Observable.from(asyncHttpClient.executeRequest(request, handler));
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
        BoundRequestBuilder requestBuilder = asyncHttpClient.preparePost(url);
        requestBuilder.setCookies(cookies);
        requestBuilder.setFollowRedirect(true);
        requestBuilder.setProxyServer(proxyManager.getProxyServer());

        Request request = requestBuilder.build();
        handler.setProxyManager(proxyManager);

        Set<Map.Entry<String, String>> entries = formParameters.entrySet();
        for (Map.Entry<String, String> e : entries) {
            requestBuilder.addFormParam(e.getKey(), e.getValue());
        }
        return Observable.from(asyncHttpClient.executeRequest(request, handler));
    }

    /**
     * Close HTTP client
     */
    public void close() throws java.io.IOException {
        asyncHttpClient.close();
    }

    private class StatsRecodringCompletionHandlerWrapper<R> extends AbstractCompletionHandler<R> {
        private int requestSize;
        private AbstractCompletionHandler<R> handler;
//        private Sensor requestSizeSensor;
//        private Sensor responseSizeSensor;
//        private Sensor requestLatencySensor;
        private long started;
        private long responseSize;


        StatsRecodringCompletionHandlerWrapper(int requestSize, AbstractCompletionHandler<R> handler) {
            this.requestSize = requestSize;
            this.handler = handler;
        }

        @Override
        public State onHeadersReceived(HttpResponseHeaders headers) throws Exception {
            started = System.nanoTime();
            return super.onHeadersReceived(headers);
        }

        @Override
        public State onBodyPartReceived(HttpResponseBodyPart content) throws Exception {
            responseSize += content.length();
            return super.onBodyPartReceived(content);
        }

        @Override
        public R onCompleted(Response response) throws Exception {
            finished(requestSize, responseSize, System.nanoTime() - started);
            return handler.onCompleted(response);
        }

        /**
         * Indicate that a request has finished successfully.
         */
        void finished(long requestSize, long responseSize, long latencyMs) {
//            requestSizeSensor.record(requestSize);
//            responseSizeSensor.record(responseSize);
//            requestLatencySensor.record(latencyMs);
        }
    }
}
