package com.naxsoft.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration for Elasticsearch client.
 * Provides configuration properties for Elasticsearch alongside existing Dagger configuration.
 * <p>
 * This configuration:
 * - Uses @ConfigurationProperties for type-safe configuration
 * - Provides baseline for future Elasticsearch client beans
 * - Can be disabled via properties for testing
 * <p>
 * TODO: Phase 2 - Add actual Elasticsearch client beans when storage module is migrated
 */
@Configuration
@EnableConfigurationProperties(CrawlerProperties.class)
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "crawler.elasticsearch", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ElasticsearchConfiguration {

    private final CrawlerProperties crawlerProperties;

    // TODO: Phase 2 - Add Elasticsearch client beans here
    // For now, this class just provides the configuration properties

    /**
     * Log Elasticsearch configuration on startup
     */
    @jakarta.annotation.PostConstruct
    public void logConfiguration() {
        CrawlerProperties.Elasticsearch esConfig = crawlerProperties.getElasticsearch();
        log.info("Elasticsearch configuration loaded: {}:{} (index: {})",
                esConfig.getHost(), esConfig.getPort(), esConfig.getIndexName());
    }
}