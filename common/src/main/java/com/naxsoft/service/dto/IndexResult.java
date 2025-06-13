package com.naxsoft.service.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Immutable result object for indexing operations.
 * Provides comprehensive feedback about index operations.
 */
@Value
@Builder(toBuilder = true)
public class IndexResult {

    @Builder.Default
    boolean successful = true;

    @Builder.Default
    int indexedCount = 0;

    @Builder.Default
    int errorCount = 0;

    @Builder.Default
    Duration duration = Duration.ZERO;

    List<String> errors;

    @Builder.Default
    Instant completedAt = Instant.now();

    /**
     * Create a successful indexing result
     */
    public static IndexResult success(int indexedCount, Duration duration) {
        return IndexResult.builder()
                .successful(true)
                .indexedCount(indexedCount)
                .duration(duration)
                .build();
    }

    /**
     * Create a failed indexing result
     */
    public static IndexResult failure(List<String> errors) {
        return IndexResult.builder()
                .successful(false)
                .errorCount(errors != null ? errors.size() : 1)
                .errors(errors)
                .build();
    }

    /**
     * Create a partial success result (some succeeded, some failed)
     */
    public static IndexResult partial(int indexedCount, int errorCount, Duration duration, List<String> errors) {
        return IndexResult.builder()
                .successful(errorCount == 0)
                .indexedCount(indexedCount)
                .errorCount(errorCount)
                .duration(duration)
                .errors(errors)
                .build();
    }

    /**
     * Calculate the success rate as a percentage
     *
     * @return success rate between 0.0 and 1.0
     */
    public double getSuccessRate() {
        int totalAttempts = indexedCount + errorCount;
        if (totalAttempts == 0) {
            return 1.0;
        }
        return (double) indexedCount / totalAttempts;
    }

    /**
     * Check if the indexing had any errors
     *
     * @return true if there were errors during indexing
     */
    public boolean hasErrors() {
        return errorCount > 0 || (errors != null && !errors.isEmpty());
    }

    /**
     * Get the indexing rate in documents per second
     *
     * @return indexing rate
     */
    public double getIndexingRate() {
        if (duration.isZero() || indexedCount == 0) {
            return 0.0;
        }
        return (double) indexedCount / duration.getSeconds();
    }
}