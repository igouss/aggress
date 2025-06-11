package com.naxsoft.modules;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.AhcHttpClient;
import com.naxsoft.crawler.HttpClient;
import dagger.Module;
import dagger.Provides;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.net.ssl.SSLException;
import javax.validation.constraints.NotNull;


@Module(includes = {MetricsRegistryModule.class})
public class HttpClientModule {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpClientModule.class);

    @Provides
    @Singleton
    @NotNull
    static HttpClient provideHttpClient(MetricRegistry registry) {
        HttpClient httpClient;

        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
            SslContext sslContext = sslContextBuilder.build();
            httpClient = new AhcHttpClient(registry, sslContext);
        } catch (SSLException e) {
            LOGGER.error("Failed to initialize HttpClientModule", e);
            throw new RuntimeException("Failed to initialize HttpClientModule", e);

        }
        return httpClient;
    }
}
