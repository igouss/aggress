package com.naxsoft.modules;

import com.naxsoft.storage.elasticsearch.Elastic;
import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Boot configuration for Elasticsearch services.
 * Replaces Dagger ElasticModule with Spring native dependency injection.
 */
@Configuration
@Slf4j
public class ElasticModule {

    @Bean
    public Elastic elastic() {
        log.info("Creating Elasticsearch connection");
        Elastic elastic = new Elastic();
        try {
            String elasticHost = AppProperties.getProperty("elasticHost");
            int elasticPort = Integer.parseInt(AppProperties.getProperty("elasticPort"));
            elastic.connect(elasticHost, elasticPort);
            log.info("Connected to Elasticsearch at {}:{}", elasticHost, elasticPort);
            return elastic;
        } catch (PropertyNotFoundException e) {
            log.error("Failed to load Elasticsearch properties: {}", e.getMessage(), e);
            throw new RuntimeException("Elasticsearch configuration error", e);
        }
    }
}
