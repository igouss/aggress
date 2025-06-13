package com.naxsoft.config;

import com.naxsoft.service.CacheService;
import com.naxsoft.service.SearchService;
import com.naxsoft.service.impl.ElasticsearchSearchService;
import com.naxsoft.service.impl.RedisCacheService;
import com.naxsoft.storage.elasticsearch.Elastic;
import com.naxsoft.storage.redis.RedisDatabase;
import com.naxsoft.utils.PropertyNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring Boot configuration for storage services.
 * Provides Redis and Elasticsearch clients as Spring beans along with service interfaces.
 */
@Configuration
@Slf4j
public class StorageConfiguration {

    @Value("${storage.elasticsearch.host:localhost}")
    private String elasticsearchHost;

    @Value("${storage.elasticsearch.port:9200}")
    private int elasticsearchPort;

    /**
     * Creates and configures the Elasticsearch client bean.
     * Automatically connects to the configured Elasticsearch cluster.
     *
     * @return Configured Elastic client
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "storage.elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
    public Elastic elasticsearchClient() {
        log.info("Configuring Elasticsearch client for {}:{}", elasticsearchHost, elasticsearchPort);

        Elastic elastic = new Elastic();
        elastic.connect(elasticsearchHost, elasticsearchPort);

        log.info("Elasticsearch client configured successfully");
        return elastic;
    }

    /**
     * Creates and configures the Redis client bean.
     * Uses properties from config.properties file for connection details.
     *
     * @return Configured Redis client
     * @throws PropertyNotFoundException if Redis configuration is missing
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "storage.redis.enabled", havingValue = "true", matchIfMissing = true)
    public RedisDatabase redisClient() throws PropertyNotFoundException {
        log.info("Configuring Redis client from application properties");

        RedisDatabase redis = new RedisDatabase();

        log.info("Redis client configured successfully");
        return redis;
    }

    /**
     * Creates the search service implementation using Elasticsearch.
     *
     * @param elasticsearchClient The Elasticsearch client bean
     * @return Search service implementation
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "storage.elasticsearch.enabled", havingValue = "true", matchIfMissing = true)
    public SearchService searchService(Elastic elasticsearchClient) {
        log.info("Configuring Elasticsearch search service");
        return new ElasticsearchSearchService(elasticsearchClient);
    }

    /**
     * Creates the cache service implementation using Redis.
     *
     * @param redisClient The Redis client bean
     * @return Cache service implementation
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "storage.redis.enabled", havingValue = "true", matchIfMissing = true)
    public CacheService cacheService(RedisDatabase redisClient) {
        log.info("Configuring Redis cache service");
        return new RedisCacheService(redisClient);
    }
}