package com.naxsoft.modules;

import com.naxsoft.storage.Persistent;
import com.naxsoft.storage.redis.RedisDatabase;
import com.naxsoft.utils.PropertyNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration for Redis storage services.
 * Replaces Dagger RedisModule with Spring native dependency injection.
 */
@Configuration
@Slf4j
public class RedisModule {

    @Bean
    public Persistent redisDatabase() {
        try {
            log.info("Creating Redis database connection");
            return new RedisDatabase();
        } catch (PropertyNotFoundException e) {
            log.error("Failed to create Redis database connection", e);
            throw new RuntimeException("Redis configuration error", e);
        }
    }
}
