package com.naxsoft.modules;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.AhcHttpClient;
import com.naxsoft.crawler.HttpClient;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import javax.net.ssl.SSLException;

/**
 * Spring Boot configuration for HTTP client services.
 * Replaces Dagger HttpClientModule with Spring native dependency injection.
 */
@Configuration
@Import(MetricsRegistryModule.class)
@Slf4j
public class HttpClientModule {

    @Bean
    public HttpClient httpClient(MetricRegistry metricRegistry) {
        log.info("Creating HTTP client with SSL support");
        try {
            SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
            SslContext sslContext = sslContextBuilder.build();
            HttpClient httpClient = new AhcHttpClient(metricRegistry, sslContext);
            log.info("HTTP client created successfully");
            return httpClient;
        } catch (SSLException e) {
            log.error("Failed to initialize HTTP client with SSL context", e);
            throw new RuntimeException("Failed to initialize HTTP client", e);
        }
    }
}
