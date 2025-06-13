package com.naxsoft.modules;

import io.lettuce.core.event.DefaultEventBus;
import io.lettuce.core.event.EventBus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Schedulers;

/**
 * Spring Boot configuration for Redis event bus services.
 * Replaces Dagger EventBusModule with Spring native dependency injection.
 */
@Configuration
@Slf4j
public class EventBusModule {

    @Bean
    public EventBus eventBus() {
        log.info("Creating Redis event bus with parallel scheduler");
        return new DefaultEventBus(Schedulers.parallel());
    }
}
