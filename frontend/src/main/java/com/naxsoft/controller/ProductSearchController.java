package com.naxsoft.controller;

import com.naxsoft.service.CacheService;
import com.naxsoft.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * REST controller demonstrating integration with the new Spring Boot storage services.
 * Provides API endpoints for product search and cache operations.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@ConditionalOnBean({SearchService.class, CacheService.class})
@Slf4j
public class ProductSearchController {

    private final SearchService searchService;
    private final CacheService cacheService;

    /**
     * Health check endpoint for storage services.
     *
     * @return Health status of search and cache services
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("search", searchService.isHealthy());
        health.put("cache", cacheService.isHealthy());
        health.put("timestamp", System.currentTimeMillis());

        boolean overallHealth = searchService.isHealthy() && cacheService.isHealthy();

        log.debug("Storage health check - Search: {}, Cache: {}",
                searchService.isHealthy(), cacheService.isHealthy());

        return overallHealth ?
                ResponseEntity.ok(health) :
                ResponseEntity.status(503).body(health);
    }

    /**
     * Get statistics about cached data.
     *
     * @return Cache statistics
     */
    @GetMapping("/cache/stats")
    public Mono<ResponseEntity<Map<String, Object>>> cacheStats() {
        Map<String, Object> stats = new HashMap<>();

        return Mono.zip(
                cacheService.getUnparsedCount("FRONT_PAGE").timeout(Duration.ofSeconds(5)),
                cacheService.getUnparsedCount("PRODUCT_LIST").timeout(Duration.ofSeconds(5)),
                cacheService.getUnparsedCount("PRODUCT").timeout(Duration.ofSeconds(5)),
                cacheService.getProducts().count().timeout(Duration.ofSeconds(5))
        ).map(tuple -> {
            stats.put("unparsedFrontPages", tuple.getT1());
            stats.put("unparsedProductLists", tuple.getT2());
            stats.put("unparsedProducts", tuple.getT3());
            stats.put("totalProducts", tuple.getT4());
            return ResponseEntity.ok(stats);
        }).onErrorReturn(ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve cache statistics")));
    }

    /**
     * Create product search index (admin operation).
     *
     * @param indexName Optional index name (defaults to 'products')
     * @param type      Optional document type (defaults to 'guns')
     * @return Index creation result
     */
    @GetMapping("/admin/create-index")
    public Mono<ResponseEntity<Map<String, Object>>> createIndex(
            @RequestParam(defaultValue = "products") String indexName,
            @RequestParam(defaultValue = "guns") String type) {

        return searchService.createProductIndex(indexName, type)
                .timeout(Duration.ofSeconds(30))
                .map(success -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", success);
                    result.put("indexName", indexName);
                    result.put("type", type);
                    log.info("Created search index: {} with type: {}", indexName, type);
                    return ResponseEntity.ok(result);
                })
                .onErrorResume(throwable -> {
                    log.error("Failed to create search index", throwable);
                    Map<String, Object> result = new HashMap<>();
                    result.put("error", "Failed to create index: " + throwable.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(result));
                });
    }

    /**
     * Demo endpoint showing Reactor integration.
     *
     * @param count Number of products to retrieve
     * @return Product URLs
     */
    @GetMapping("/demo/products")
    public Mono<ResponseEntity<Map<String, Object>>> demoProducts(
            @RequestParam(defaultValue = "10") int count) {

        return cacheService.getProducts()
                .take(count)
                .map(product -> product.getUrl())
                .collectList()
                .timeout(Duration.ofSeconds(10))
                .map(urls -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("productUrls", urls);
                    result.put("count", urls.size());
                    return ResponseEntity.ok(result);
                })
                .onErrorResume(throwable -> {
                    log.error("Failed to retrieve demo products", throwable);
                    Map<String, Object> result = new HashMap<>();
                    result.put("error", "Failed to retrieve products: " + throwable.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(result));
                });
    }
}