package com.naxsoft.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * Spring Boot auto-configuration for the storage module.
 * Automatically configures storage services when the module is on the classpath.
 */
@AutoConfiguration
@ConditionalOnClass({
        com.naxsoft.storage.elasticsearch.Elastic.class,
        com.naxsoft.storage.redis.RedisDatabase.class
})
@Import(StorageConfiguration.class)
@ComponentScan(basePackages = {
        "com.naxsoft.storage",
        "com.naxsoft.service"
})
@Slf4j
public class StorageAutoConfiguration {

    public StorageAutoConfiguration() {
        log.info("Storage module auto-configuration activated");
    }
}