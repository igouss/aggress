package com.naxsoft.service.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable statistics object for crawl operations.
 * Provides comprehensive metrics about the crawler system state.
 */
@Value
@Builder(toBuilder = true)
public class CrawlStats {

    @Builder.Default
    long totalPagesInQueue = 0;

    @Builder.Default
    long parsedPages = 0;

    @Builder.Default
    long unparsedPages = 0;

    @Builder.Default
    long totalProducts = 0;

    @Builder.Default
    long indexedProducts = 0;

    @Builder.Default
    long errorCount = 0;

    @Builder.Default
    Instant lastCrawlTime = null;

    @Builder.Default
    Instant lastIndexTime = null;

    Map<String, Long> pagesByType;
    Map<String, Long> productsByCategory;
    Map<String, Long> errorsByType;

    /**
     * Create empty stats for initialization
     */
    public static CrawlStats empty() {
        return CrawlStats.builder().build();
    }

    /**
     * Calculate the parsing completion percentage
     *
     * @return percentage between 0.0 and 100.0
     */
    public double getParsingProgress() {
        if (totalPagesInQueue == 0) {
            return 100.0;
        }
        return (double) parsedPages / totalPagesInQueue * 100.0;
    }

    /**
     * Calculate the indexing completion percentage
     *
     * @return percentage between 0.0 and 100.0
     */
    public double getIndexingProgress() {
        if (totalProducts == 0) {
            return 100.0;
        }
        return (double) indexedProducts / totalProducts * 100.0;
    }

    /**
     * Get the error rate as a percentage
     *
     * @return error rate between 0.0 and 100.0
     */
    public double getErrorRate() {
        long totalOperations = parsedPages + errorCount;
        if (totalOperations == 0) {
            return 0.0;
        }
        return (double) errorCount / totalOperations * 100.0;
    }

    /**
     * Check if the crawler is healthy (low error rate)
     *
     * @return true if error rate is below 5%
     */
    public boolean isHealthy() {
        return getErrorRate() < 5.0;
    }
}