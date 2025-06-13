package com.naxsoft.config;

import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration adapter that bridges between legacy AppProperties and new Spring Boot configuration.
 * During the migration, this class helps ensure both configuration approaches work together.
 * <p>
 * This adapter:
 * - Validates that both configuration systems have consistent values
 * - Provides fallback logic during transition period
 * - Logs configuration differences for debugging
 * - Will be removed once migration to Spring Boot configuration is complete
 */
@Configuration
@EnableConfigurationProperties(CrawlerProperties.class)
@RequiredArgsConstructor
@Slf4j
public class ConfigurationAdapter {

    private final CrawlerProperties crawlerProperties;

    @PostConstruct
    public void validateConfiguration() {
        log.info("Validating configuration consistency between AppProperties and Spring Boot configuration...");

        validateElasticsearchConfig();
        validateRedisConfig();
        validateAuthenticationConfig();

        log.info("Configuration validation completed successfully");
    }

    private void validateElasticsearchConfig() {
        try {
            // Check legacy AppProperties
            String legacyHost = AppProperties.getProperty("elasticHost");
            String legacyPort = AppProperties.getProperty("elasticPort");

            // Check Spring Boot properties
            String springHost = crawlerProperties.getElasticsearch().getHost();
            int springPort = crawlerProperties.getElasticsearch().getPort();

            if (!legacyHost.equals(springHost)) {
                log.warn("Elasticsearch host mismatch: AppProperties={}, SpringBoot={}", legacyHost, springHost);
            }

            if (!legacyPort.equals(String.valueOf(springPort))) {
                log.warn("Elasticsearch port mismatch: AppProperties={}, SpringBoot={}", legacyPort, springPort);
            }

            log.info("Elasticsearch configuration: {}:{}", springHost, springPort);

        } catch (PropertyNotFoundException e) {
            log.info("Legacy Elasticsearch properties not found, using Spring Boot configuration only");
        }
    }

    private void validateRedisConfig() {
        try {
            // Check legacy AppProperties
            String legacyHost = AppProperties.getProperty("redisHost");
            String legacyPort = AppProperties.getProperty("redisPort");

            // Check Spring Boot properties
            String springHost = crawlerProperties.getRedis().getHost();
            int springPort = crawlerProperties.getRedis().getPort();

            if (!legacyHost.equals(springHost)) {
                log.warn("Redis host mismatch: AppProperties={}, SpringBoot={}", legacyHost, springHost);
            }

            if (!legacyPort.equals(String.valueOf(springPort))) {
                log.warn("Redis port mismatch: AppProperties={}, SpringBoot={}", legacyPort, springPort);
            }

            log.info("Redis configuration: {}:{}", springHost, springPort);

        } catch (PropertyNotFoundException e) {
            log.info("Legacy Redis properties not found, using Spring Boot configuration only");
        }
    }

    private void validateAuthenticationConfig() {
        try {
            // Check legacy AppProperties
            String legacyLogin = AppProperties.getProperty("canadiangunnutzLogin");

            // Check Spring Boot properties
            String springLogin = crawlerProperties.getAuthentication().getCanadiangunnutzLogin();

            if (legacyLogin != null && springLogin != null && !legacyLogin.equals(springLogin)) {
                log.warn("CanadianGunNutz login mismatch between configurations");
            }

            boolean hasCredentials = (springLogin != null && !springLogin.isEmpty());
            log.info("CanadianGunNutz authentication configured: {}", hasCredentials);

        } catch (PropertyNotFoundException e) {
            log.info("Legacy authentication properties not found, using Spring Boot configuration only");
        }
    }

    /**
     * Get Elasticsearch host with fallback logic
     */
    public String getElasticsearchHost() {
        try {
            return AppProperties.getProperty("elasticHost");
        } catch (PropertyNotFoundException e) {
            return crawlerProperties.getElasticsearch().getHost();
        }
    }

    /**
     * Get Redis host with fallback logic
     */
    public String getRedisHost() {
        try {
            return AppProperties.getProperty("redisHost");
        } catch (PropertyNotFoundException e) {
            return crawlerProperties.getRedis().getHost();
        }
    }
}