package com.naxsoft.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

/**
 * Spring Boot configuration properties for the Aggress Web Crawler.
 * Provides type-safe, validated configuration management as an alternative to AppProperties.
 * <p>
 * Usage:
 * - Configure via application.yml or application.properties
 * - Inject into Spring components via constructor injection
 * - Validation happens at startup time
 */
@Data
@ConfigurationProperties(prefix = "crawler")
@Validated
public class CrawlerProperties {

    /**
     * Core crawler configuration
     */
    @Valid
    @NotNull
    private Core core = new Core();

    /**
     * Elasticsearch configuration
     */
    @Valid
    @NotNull
    private Elasticsearch elasticsearch = new Elasticsearch();

    /**
     * Redis configuration
     */
    @Valid
    @NotNull
    private Redis redis = new Redis();

    /**
     * Tor proxy configuration for anonymous crawling
     */
    @Valid
    @NotNull
    private TorProxy torProxy = new TorProxy();

    /**
     * Site-specific authentication configuration
     */
    @Valid
    @NotNull
    private Authentication authentication = new Authentication();

    @Data
    public static class Core {
        /**
         * Deployment environment (development, staging, production)
         */
        @NotBlank
        private String deploymentEnv = "development";

        /**
         * Number of concurrent crawl threads
         */
        private int crawlThreads = 5;

        /**
         * Request timeout duration
         */
        private Duration requestTimeout = Duration.ofMinutes(2);

        /**
         * Enable debug logging
         */
        private boolean debugLogging = false;
    }

    @Data
    public static class Elasticsearch {
        /**
         * Elasticsearch host URL
         */
        @NotBlank
        private String host = "localhost";

        /**
         * Elasticsearch port
         */
        private int port = 9200;
        /**
         * Index name for products
         */
        @NotBlank
        private String indexName = "products";
        /**
         * Bulk indexing batch size
         */
        private int batchSize = 100;
        /**
         * Connection timeout
         */
        private Duration timeout = Duration.ofSeconds(30);

        /**
         * Connection URI (computed property)
         */
        public String getUri() {
            return String.format("http://%s:%d", host, port);
        }
    }

    @Data
    public static class Redis {
        /**
         * Redis host
         */
        @NotBlank
        private String host = "localhost";

        /**
         * Redis port
         */
        private int port = 6379;

        /**
         * Redis password (optional)
         */
        private String password;

        /**
         * Redis database number
         */
        private int database = 0;

        /**
         * Connection timeout
         */
        private Duration timeout = Duration.ofSeconds(5);
    }

    @Data
    public static class TorProxy {
        /**
         * Tor proxy host
         */
        @NotBlank
        private String host = "localhost";

        /**
         * Tor proxy port
         */
        private int port = 8118;

        /**
         * Enable Tor proxy usage
         */
        private boolean enabled = false;
    }

    @Data
    public static class Authentication {
        /**
         * CanadianGunNutz login credentials
         */
        private String canadiangunnutzLogin;
        private String canadiangunnutzPassword;
    }
}