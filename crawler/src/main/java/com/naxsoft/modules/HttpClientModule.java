package com.naxsoft.modules;

import com.naxsoft.http.AhcHttpClient;
import com.naxsoft.http.HttpClient;
import dagger.Module;
import dagger.Provides;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

@Module()
public class HttpClientModule {
    private final static Logger LOGGER = LoggerFactory.getLogger(HttpClientModule.class);

    @Provides
    @Singleton
    @NotNull
    static HttpClient provideHttpClient() {
        HttpClient httpClient;

        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
            SslContext sslContext = sslContextBuilder.build();
            httpClient = new AhcHttpClient();
        } catch (Exception e) {
            LOGGER.error("Failed to initialize HttpClientModule", e);
            throw new RuntimeException("Failed to initialize HttpClientModule", e);

        }
        return httpClient;
    }
}
