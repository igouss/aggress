package com.naxsoft.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.Map;

/**
 * Spring Boot 3.5 Actuator configuration with enhanced info endpoint.
 * Provides custom info contributions for the Aggress crawler system.
 */
@Configuration
@Slf4j
public class ActuatorConfiguration {

    /**
     * Custom info contributor for the actuator info endpoint.
     * Provides crawler-specific information for monitoring.
     */
    @Bean
    public InfoContributor crawlerInfoContributor() {
        return (Info.Builder builder) -> {
            builder.withDetail("crawler", Map.of(
                    "system", "Aggress Web Crawler",
                    "version", "3.5.0",
                    "spring-boot", "3.5.0",
                    "java-version", System.getProperty("java.version"),
                    "startup-time", Instant.now().toString(),
                    "features", Map.of(
                            "structured-logging", true,
                            "redis-lettuce-read-from", true,
                            "webclient-global-config", true,
                            "task-scheduling-enhancements", true,
                            "bootstrap-executor", true
                    )
            ));

            log.debug("Added crawler info to actuator endpoint");
        };
    }
}