package com.naxsoft.service.dto;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Immutable result object for crawl operations.
 * Provides comprehensive information about crawl execution and results.
 */
@Value
@Builder(toBuilder = true)
public class CrawlResult {

    @NonNull
    String crawlId;

    @Builder.Default
    boolean successful = true;

    @Builder.Default
    int processedCount = 0;

    @Builder.Default
    int errorCount = 0;

    @Builder.Default
    Duration duration = Duration.ZERO;

    List<String> errors;

    @Builder.Default
    Instant completedAt = Instant.now();

    @Builder.Default
    Instant startedAt = Instant.now();

    /**
     * Create a successful crawl result
     */
    public static CrawlResult success(String crawlId, int processedCount, Duration duration) {
        return CrawlResult.builder()
                .crawlId(crawlId)
                .successful(true)
                .processedCount(processedCount)
                .duration(duration)
                .build();
    }

    /**
     * Create a failed crawl result
     */
    public static CrawlResult failure(String crawlId, List<String> errors) {
        return CrawlResult.builder()
                .crawlId(crawlId)
                .successful(false)
                .errorCount(errors != null ? errors.size() : 1)
                .errors(errors)
                .build();
    }

    /**
     * Calculate the success rate as a percentage
     *
     * @return success rate between 0.0 and 1.0
     */
    public double getSuccessRate() {
        int totalAttempts = processedCount + errorCount;
        if (totalAttempts == 0) {
            return 1.0;
        }
        return (double) processedCount / totalAttempts;
    }

    /**
     * Check if the crawl had any errors
     *
     * @return true if there were errors during crawling
     */
    public boolean hasErrors() {
        return errorCount > 0 || (errors != null && !errors.isEmpty());
    }

    /**
     * Get the number of pages processed per second
     *
     * @return processing rate in pages/second
     */
    public double getProcessingRate() {
        if (duration.isZero() || processedCount == 0) {
            return 0.0;
        }
        return (double) processedCount / duration.getSeconds();
    }
}