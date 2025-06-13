package com.naxsoft.modules;

import io.vertx.core.Vertx;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration for Vert.x reactive framework.
 * Replaces Dagger VertxModule with Spring native dependency injection.
 */
@Configuration
@Slf4j
public class VertxModule {

    @Bean
    public Vertx vertx() {
        log.info("Creating Vert.x instance");
        // use the JVM built-in resolver
        System.setProperty("vertx.disableDnsResolver", "true");
        return Vertx.vertx();
    }
}
