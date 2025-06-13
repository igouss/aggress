package com.naxsoft.modules;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.parsers.productParser.ProductParserFactory;
import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration for product parser factory.
 * Replaces Dagger ProductParserFactoryModule with Spring native dependency injection.
 */
@Configuration
@Slf4j
public class ProductParserFactoryModule {

    @Bean
    public ProductParserFactory productParserFactory(Vertx vertx, MetricRegistry metricRegistry) {
        log.info("Creating ProductParserFactory");
        return new ProductParserFactory(vertx, metricRegistry);
    }
}