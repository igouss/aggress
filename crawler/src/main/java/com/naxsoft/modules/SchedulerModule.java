package com.naxsoft.modules;

import com.naxsoft.scheduler.Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration for crawler scheduling services.
 * Replaces Dagger SchedulerModule with Spring native dependency injection.
 */
@Configuration
@Slf4j
public class SchedulerModule {

    @Bean
    public Scheduler scheduler() {
        log.info("Creating crawler scheduler");
        return new Scheduler();
    }
}
