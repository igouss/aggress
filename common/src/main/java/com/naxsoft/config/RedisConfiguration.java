package com.naxsoft.config;

import io.lettuce.core.ReadFrom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Spring Boot 3.5 enhanced Redis configuration with Lettuce client optimizations.
 * Provides configuration properties for Redis alongside existing Dagger configuration.
 * <p>
 * This configuration:
 * - Uses @ConfigurationProperties for type-safe configuration
 * - Leverages Spring Boot 3.5 Lettuce read-from configuration
 * - Provides enhanced connection pooling and timeouts
 * - Can be disabled via properties for testing
 */
@Configuration
@EnableConfigurationProperties(CrawlerProperties.class)
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "crawler.redis", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RedisConfiguration {

    private final CrawlerProperties crawlerProperties;

    /**
     * Spring Boot 3.5 Lettuce client configuration customizer.
     * Optimizes Redis connections for crawler workloads.
     */
    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceClientConfigurationCustomizer() {
        return clientConfigurationBuilder -> {
            CrawlerProperties.Redis redisConfig = crawlerProperties.getRedis();

            clientConfigurationBuilder
                    .commandTimeout(redisConfig.getTimeout())
                    .shutdownTimeout(Duration.ofSeconds(5))
                    .useSsl()
                    .and()
                    .readFrom(ReadFrom.REPLICA_PREFERRED);

            log.info("Configured Lettuce with replica-preferred reads and {} timeout",
                    redisConfig.getTimeout());
        };
    }

    /**
     * Log Redis configuration on startup
     */
    @jakarta.annotation.PostConstruct
    public void logConfiguration() {
        CrawlerProperties.Redis redisConfig = crawlerProperties.getRedis();
        log.info("Spring Boot 3.5 Redis configuration loaded: {}:{} (database: {})",
                redisConfig.getHost(), redisConfig.getPort(), redisConfig.getDatabase());
        log.info("Lettuce read-from strategy: replica-preferred for improved performance");
    }
}