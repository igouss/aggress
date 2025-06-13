package com.naxsoft.modules;

import com.codahale.metrics.MetricRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration for metrics collection.
 * Replaces Dagger MetricsRegistryModule with Spring native dependency injection.
 */
@Configuration
@Slf4j
public class MetricsRegistryModule {

    @Bean
    public MetricRegistry metricRegistry() {
        log.info("Creating MetricRegistry for application monitoring");
        return new MetricRegistry();
    }
}
