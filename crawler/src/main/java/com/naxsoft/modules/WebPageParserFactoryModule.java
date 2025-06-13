package com.naxsoft.modules;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.parsers.webPageParsers.WebPageParserFactory;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration for web page parser factory.
 * Replaces Dagger WebPageParserFactoryModule with Spring native dependency injection.
 */
@Configuration
@Slf4j
public class WebPageParserFactoryModule {

    @Bean
    public WebPageParserFactory webPageParserFactory(Vertx vertx, HttpClient httpClient, MetricRegistry metricRegistry) {
        log.info("Creating WebPageParserFactory");
        return new WebPageParserFactory(vertx, httpClient, metricRegistry);
    }
}