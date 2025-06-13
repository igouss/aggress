package com.naxsoft.service.dto;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.Duration;
import java.util.Set;

/**
 * Immutable request object for crawl operations.
 * Uses Lombok for clean, readable object construction and validation.
 */
@Value
@Builder(toBuilder = true)
public class CrawlRequest {

    @NonNull
    PageType pageType;

    @Builder.Default
    int limit = 100;

    @Builder.Default
    Duration timeout = Duration.ofMinutes(5);

    Set<String> includeDomains;
    Set<String> excludeDomains;

    @Builder.Default
    boolean forceRefresh = false;

    @Builder.Default
    int maxRetries = 3;

    /**
     * Create a simple crawl request for a specific page type
     */
    public static CrawlRequest forPageType(PageType pageType) {
        return CrawlRequest.builder()
                .pageType(pageType)
                .build();
    }

    /**
     * Create a crawl request with custom limit
     */
    public static CrawlRequest withLimit(PageType pageType, int limit) {
        return CrawlRequest.builder()
                .pageType(pageType)
                .limit(limit)
                .build();
    }
}