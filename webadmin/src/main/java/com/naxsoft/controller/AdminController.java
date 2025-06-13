package com.naxsoft.controller;

import com.naxsoft.service.CacheService;
import com.naxsoft.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Admin REST controller providing administrative operations for the crawler system.
 * Integrates with the new Spring Boot storage services for administrative tasks.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@ConditionalOnBean({SearchService.class, CacheService.class})
@Slf4j
public class AdminController {

    private final SearchService searchService;
    private final CacheService cacheService;

    /**
     * System status dashboard showing overall health and statistics.
     *
     * @return System status information
     */
    @GetMapping("/status")
    public Mono<ResponseEntity<Map<String, Object>>> systemStatus() {
        Map<String, Object> status = new HashMap<>();

        // Service health
        Map<String, Boolean> services = new HashMap<>();
        services.put("elasticsearch", searchService.isHealthy());
        services.put("redis", cacheService.isHealthy());
        status.put("services", services);

        return Mono.zip(
                cacheService.getUnparsedCount("FRONT_PAGE").timeout(Duration.ofSeconds(5)),
                cacheService.getUnparsedCount("PRODUCT_LIST").timeout(Duration.ofSeconds(5)),
                cacheService.getUnparsedCount("PRODUCT").timeout(Duration.ofSeconds(5)),
                cacheService.getProducts().count().timeout(Duration.ofSeconds(5))
        ).map(tuple -> {
            Map<String, Object> cache = new HashMap<>();
            cache.put("unparsedFrontPages", tuple.getT1());
            cache.put("unparsedProductLists", tuple.getT2());
            cache.put("unparsedProducts", tuple.getT3());
            cache.put("totalProducts", tuple.getT4());

            status.put("cache", cache);
            status.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(status);
        }).onErrorResume(throwable -> {
            log.error("Failed to get cache statistics", throwable);
            Map<String, Object> cache = new HashMap<>();
            cache.put("error", "Failed to retrieve statistics");
            status.put("cache", cache);
            status.put("timestamp", System.currentTimeMillis());
            return Mono.just(ResponseEntity.ok(status));
        });
    }

    /**
     * Initialize search indices (admin operation).
     *
     * @param indexName Index name to create
     * @param type      Document type
     * @return Creation result
     */
    @PostMapping("/search/init")
    public Mono<ResponseEntity<Map<String, Object>>> initializeSearch(
            @RequestParam(defaultValue = "products") String indexName,
            @RequestParam(defaultValue = "guns") String type) {

        return searchService.createProductIndex(indexName, type)
                .timeout(Duration.ofMinutes(1))
                .map(success -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", success);
                    result.put("indexName", indexName);
                    result.put("type", type);
                    result.put("message", success ?
                            "Index created successfully" :
                            "Index creation failed");
                    log.info("Admin initiated search index creation: {}/{}", indexName, type);
                    return ResponseEntity.ok(result);
                })
                .onErrorResume(throwable -> {
                    log.error("Failed to initialize search index", throwable);
                    Map<String, Object> result = new HashMap<>();
                    result.put("error", "Failed to create index: " + throwable.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(result));
                });
    }

    /**
     * Clean up cache data (admin operation).
     *
     * @param tables Optional array of table names to clean
     * @return Cleanup result
     */
    @PostMapping("/cache/cleanup")
    public Mono<ResponseEntity<Map<String, Object>>> cleanupCache(
            @RequestParam(required = false) String[] tables) {

        String[] tablesToClean = (tables != null && tables.length > 0) ?
                tables :
                new String[]{"WebPageEntity", "ProductEntity"};

        return cacheService.cleanUp(tablesToClean)
                .timeout(Duration.ofSeconds(30))
                .map(cleanupResult -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("cleanedTables", tablesToClean);
                    result.put("result", cleanupResult);
                    log.info("Admin initiated cache cleanup for tables: {}", java.util.Arrays.toString(tablesToClean));
                    return ResponseEntity.ok(result);
                })
                .onErrorResume(throwable -> {
                    log.error("Failed to cleanup cache", throwable);
                    Map<String, Object> result = new HashMap<>();
                    result.put("error", "Failed to cleanup cache: " + throwable.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(result));
                });
    }

    /**
     * Get detailed crawl queue information.
     *
     * @return Queue statistics by type
     */
    @GetMapping("/queue/stats")
    public Mono<ResponseEntity<Map<String, Object>>> queueStats() {
        return Mono.zip(
                cacheService.getUnparsedCount("FRONT_PAGE").timeout(Duration.ofSeconds(5)).onErrorReturn(-1L),
                cacheService.getUnparsedCount("PRODUCT_LIST").timeout(Duration.ofSeconds(5)).onErrorReturn(-1L),
                cacheService.getUnparsedCount("PRODUCT").timeout(Duration.ofSeconds(5)).onErrorReturn(-1L)
        ).map(tuple -> {
            Map<String, Object> stats = new HashMap<>();
            stats.put("front_page", tuple.getT1());
            stats.put("product_list", tuple.getT2());
            stats.put("product", tuple.getT3());
            return ResponseEntity.ok(stats);
        });
    }

    /**
     * Preview unparsed items from queue (for debugging).
     *
     * @param type  Page type to preview
     * @param count Number of items to preview
     * @return Preview of unparsed items
     */
    @GetMapping("/queue/preview")
    public Mono<ResponseEntity<Map<String, Object>>> previewQueue(
            @RequestParam String type,
            @RequestParam(defaultValue = "5") int count) {

        return cacheService.getUnparsedByType(type, (long) count)
                .map(webPage -> Map.of(
                        "url", webPage.getUrl(),
                        "type", webPage.getType(),
                        "category", webPage.getCategory()
                ))
                .collectList()
                .timeout(Duration.ofSeconds(10))
                .map(items -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("type", type);
                    result.put("count", items.size());
                    result.put("items", items);
                    return ResponseEntity.ok(result);
                })
                .onErrorResume(throwable -> {
                    log.error("Failed to preview queue for type: {}", type, throwable);
                    Map<String, Object> result = new HashMap<>();
                    result.put("error", "Failed to preview queue: " + throwable.getMessage());
                    return Mono.just(ResponseEntity.status(500).body(result));
                });
    }
}