package com.naxsoft.service.impl;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.service.CacheService;
import com.naxsoft.storage.redis.RedisDatabase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Redis implementation of the CacheService interface.
 * Delegates to the RedisDatabase component for actual Redis operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService implements CacheService {

    private final RedisDatabase redisClient;

    @Override
    public Mono<Long> markWebPageAsParsed(WebPageEntity webPageEntity) {
        log.debug("Marking web page as parsed: {}", webPageEntity.getUrl());
        return redisClient.markWebPageAsParsed(webPageEntity);
    }

    @Override
    public Mono<Long> addProductPageEntry(ProductEntity productEntity) {
        log.debug("Adding product page entry: {}", productEntity.getUrl());
        return redisClient.addProductPageEntry(productEntity);
    }

    @Override
    public Mono<Long> addWebPageEntry(WebPageEntity webPageEntity) {
        log.debug("Adding web page entry: {} (type: {})", webPageEntity.getUrl(), webPageEntity.getType());
        return redisClient.addWebPageEntry(webPageEntity);
    }

    @Override
    public Flux<ProductEntity> getProducts() {
        log.debug("Retrieving all products from cache");
        return redisClient.getProducts();
    }

    @Override
    public Mono<Long> getUnparsedCount(String type) {
        log.debug("Getting unparsed count for type: {}", type);
        return redisClient.getUnparsedCount(type);
    }

    @Override
    public Flux<WebPageEntity> getUnparsedByType(String type, Long count) {
        log.debug("Getting {} unparsed pages of type: {}", count, type);
        return redisClient.getUnparsedByType(type, count);
    }

    @Override
    public Mono<String> cleanUp(String[] tables) {
        log.debug("Cleaning up cache tables: {}", java.util.Arrays.toString(tables));
        return redisClient.cleanUp(tables);
    }

    @Override
    public boolean isHealthy() {
        try {
            // Simple health check - could be enhanced with actual Redis ping
            return redisClient != null;
        } catch (Exception e) {
            log.warn("Redis health check failed", e);
            return false;
        }
    }
}