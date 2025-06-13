package com.naxsoft;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Spring Boot application entry point for the Aggress WebAdmin service.
 * Runs alongside the existing Kotlin-based WebAdmin main class during migration.
 * <p>
 * This provides:
 * - Spring Boot web framework (alternative to Vert.x)
 * - Health checks via Actuator
 * - Configuration management via @ConfigurationProperties
 * - Auto-configuration for admin services
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@Slf4j
public class WebAdminApplication {

    public static void main(String[] args) {
        log.info("Starting Aggress WebAdmin with Spring Boot...");

        // Configure Spring Boot to run on port 8083 (alongside existing webadmin on 8081)
        System.setProperty("server.port", "8083");

        ConfigurableApplicationContext context = SpringApplication.run(WebAdminApplication.class, args);

        log.info("Spring Boot WebAdmin started successfully on port 8083");
        log.info("Existing Kotlin webadmin continues to run on port 8081");
        log.info("Available beans: {}", context.getBeanDefinitionCount());

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down Aggress WebAdmin Spring Boot application...");
            context.close();
        }));
    }
}