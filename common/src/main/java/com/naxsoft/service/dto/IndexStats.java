package com.naxsoft.service.dto;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Immutable statistics object for search index metrics.
 * Provides comprehensive information about index health and performance.
 */
@Value
@Builder(toBuilder = true)
public class IndexStats {

    @Builder.Default
    long documentCount = 0;

    @Builder.Default
    long indexSizeBytes = 0;

    @Builder.Default
    int shardCount = 1;

    @Builder.Default
    int replicaCount = 0;

    @Builder.Default
    boolean indexHealthy = true;

    String indexStatus; // green, yellow, red

    @Builder.Default
    Instant lastIndexTime = null;

    @Builder.Default
    Instant createdAt = null;

    @Builder.Default
    double avgIndexingRate = 0.0; // docs per second

    @Builder.Default
    double avgSearchLatency = 0.0; // milliseconds

    /**
     * Create empty stats for initialization
     */
    public static IndexStats empty() {
        return IndexStats.builder()
                .indexStatus("unknown")
                .build();
    }

    /**
     * Create healthy stats
     */
    public static IndexStats healthy(long documentCount, long indexSizeBytes) {
        return IndexStats.builder()
                .documentCount(documentCount)
                .indexSizeBytes(indexSizeBytes)
                .indexHealthy(true)
                .indexStatus("green")
                .createdAt(Instant.now())
                .build();
    }

    /**
     * Get index size in human-readable format
     *
     * @return formatted size string (e.g., "1.2 MB", "3.4 GB")
     */
    public String getFormattedSize() {
        if (indexSizeBytes < 1024) {
            return indexSizeBytes + " B";
        } else if (indexSizeBytes < 1024 * 1024) {
            return String.format("%.1f KB", indexSizeBytes / 1024.0);
        } else if (indexSizeBytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", indexSizeBytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", indexSizeBytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Check if the index is in a good state
     *
     * @return true if index is healthy and status is green
     */
    public boolean isHealthy() {
        return indexHealthy && "green".equalsIgnoreCase(indexStatus);
    }

    /**
     * Check if the index has good performance
     *
     * @return true if search latency is reasonable
     */
    public boolean hasGoodPerformance() {
        return avgSearchLatency < 100.0; // Less than 100ms average
    }

    /**
     * Get the average document size in bytes
     *
     * @return average document size, or 0 if no documents
     */
    public double getAverageDocumentSize() {
        if (documentCount == 0) {
            return 0.0;
        }
        return (double) indexSizeBytes / documentCount;
    }
}