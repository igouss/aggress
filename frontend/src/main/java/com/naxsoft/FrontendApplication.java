package com.naxsoft;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Spring Boot application entry point for the Aggress Frontend service.
 * Runs alongside the existing Vert.x-based Server main class during migration.
 * <p>
 * This provides:
 * - Spring Boot web framework (alternative to Vert.x)
 * - Health checks via Actuator
 * - Configuration management via @ConfigurationProperties
 * - Auto-configuration for web services
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@Slf4j
public class FrontendApplication {

    public static void main(String[] args) {
        log.info("Starting Aggress Frontend with Spring Boot...");

        // Configure Spring Boot to run on port 8082 (alongside existing frontend on 8080)
        System.setProperty("server.port", "8082");

        ConfigurableApplicationContext context = SpringApplication.run(FrontendApplication.class, args);

        log.info("Spring Boot Frontend started successfully on port 8082");
        log.info("Existing Vert.x frontend continues to run on port 8080");
        log.info("Available beans: {}", context.getBeanDefinitionCount());

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down Aggress Frontend Spring Boot application...");
            context.close();
        }));
    }
}