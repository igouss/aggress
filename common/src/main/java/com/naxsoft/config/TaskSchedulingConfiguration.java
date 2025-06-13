package com.naxsoft.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.task.SimpleAsyncTaskSchedulerBuilder;
import org.springframework.boot.task.ThreadPoolTaskSchedulerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.SimpleAsyncTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Spring Boot 3.5 Task Scheduling configuration with TaskDecorator and bootstrap executor support.
 * Enhances the crawler's async processing capabilities.
 */
@Configuration
@EnableAsync
@EnableScheduling
@Slf4j
public class TaskSchedulingConfiguration {

    /**
     * Task decorator for Spring Boot 3.5 that adds logging and MDC context to scheduled tasks.
     */
    @Bean
    public TaskDecorator crawlerTaskDecorator() {
        return runnable -> {
            return () -> {
                try {
                    log.debug("Executing crawler task in thread: {}", Thread.currentThread().getName());
                    runnable.run();
                } finally {
                    log.debug("Completed crawler task in thread: {}", Thread.currentThread().getName());
                }
            };
        };
    }

    /**
     * Enhanced task scheduler using Spring Boot 3.5 ThreadPoolTaskSchedulerBuilder.
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler(ThreadPoolTaskSchedulerBuilder builder, TaskDecorator taskDecorator) {
        return builder
                .poolSize(5)
                .threadNamePrefix("aggress-scheduler-")
                .taskDecorator(taskDecorator)
                .build();
    }

    /**
     * Simple async task scheduler using Spring Boot 3.5 SimpleAsyncTaskSchedulerBuilder.
     */
    @Bean
    public SimpleAsyncTaskScheduler simpleTaskScheduler(SimpleAsyncTaskSchedulerBuilder builder, TaskDecorator taskDecorator) {
        return builder
                .threadNamePrefix("aggress-async-")
                .taskDecorator(taskDecorator)
                .build();
    }
}