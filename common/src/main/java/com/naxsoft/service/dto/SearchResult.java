package com.naxsoft.service.dto;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.Duration;
import java.util.List;

/**
 * Immutable search result object with pagination and metadata.
 * Generic type allows reuse for different entity types.
 */
@Value
@Builder(toBuilder = true)
public class SearchResult<T> {

    @NonNull
    List<T> results;

    @Builder.Default
    long totalHits = 0;

    @Builder.Default
    int page = 0;

    @Builder.Default
    int size = 10;

    @Builder.Default
    Duration queryTime = Duration.ZERO;

    String scrollId;

    float maxScore;

    @Builder.Default
    boolean timedOut = false;

    /**
     * Create an empty search result
     */
    public static <T> SearchResult<T> empty() {
        return SearchResult.<T>builder()
                .results(List.of())
                .totalHits(0)
                .build();
    }

    /**
     * Create a search result with results but no pagination info
     */
    public static <T> SearchResult<T> of(List<T> results) {
        return SearchResult.<T>builder()
                .results(results)
                .totalHits(results.size())
                .build();
    }

    /**
     * Create a paginated search result
     */
    public static <T> SearchResult<T> paginated(List<T> results, long totalHits, int page, int size, Duration queryTime) {
        return SearchResult.<T>builder()
                .results(results)
                .totalHits(totalHits)
                .page(page)
                .size(size)
                .queryTime(queryTime)
                .build();
    }

    /**
     * Check if there are more results available
     *
     * @return true if more pages exist
     */
    public boolean hasMore() {
        return (long) (page + 1) * size < totalHits;
    }

    /**
     * Get the total number of pages
     *
     * @return total page count
     */
    public int getTotalPages() {
        if (size == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalHits / size);
    }

    /**
     * Check if this is the first page
     *
     * @return true if this is page 0
     */
    public boolean isFirstPage() {
        return page == 0;
    }

    /**
     * Check if this is the last page
     *
     * @return true if this is the final page
     */
    public boolean isLastPage() {
        return !hasMore();
    }

    /**
     * Get the starting index of results on this page
     *
     * @return zero-based start index
     */
    public int getStartIndex() {
        return page * size;
    }

    /**
     * Get the ending index of results on this page
     *
     * @return zero-based end index
     */
    public int getEndIndex() {
        return Math.min(getStartIndex() + size - 1, (int) totalHits - 1);
    }
}