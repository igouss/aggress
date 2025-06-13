package com.naxsoft.service.dto;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Set;

/**
 * Immutable search query object with filters and pagination.
 * Provides type-safe search parameters for product queries.
 */
@Value
@Builder(toBuilder = true)
public class SearchQuery {

    @NonNull
    String query;

    @Builder.Default
    int page = 0;

    @Builder.Default
    int size = 10;

    Set<String> categories;
    Set<String> retailers;

    String minPrice;
    String maxPrice;

    String sortBy;

    @Builder.Default
    SortOrder sortOrder = SortOrder.ASC;

    @Builder.Default
    boolean includeOutOfStock = true;

    String retailerFilter;
    String categoryFilter;

    /**
     * Create a simple text search query
     */
    public static SearchQuery forText(String query) {
        return SearchQuery.builder()
                .query(query)
                .build();
    }

    /**
     * Create a search query with pagination
     */
    public static SearchQuery withPagination(String query, int page, int size) {
        return SearchQuery.builder()
                .query(query)
                .page(page)
                .size(size)
                .build();
    }

    /**
     * Create a search query with category filter
     */
    public static SearchQuery inCategory(String query, String category) {
        return SearchQuery.builder()
                .query(query)
                .categoryFilter(category)
                .build();
    }

    /**
     * Create a search query with price range
     */
    public static SearchQuery withPriceRange(String query, String minPrice, String maxPrice) {
        return SearchQuery.builder()
                .query(query)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();
    }

    /**
     * Create a search query sorted by specific field
     */
    public static SearchQuery sortedBy(String query, String sortBy, SortOrder order) {
        return SearchQuery.builder()
                .query(query)
                .sortBy(sortBy)
                .sortOrder(order)
                .build();
    }

    /**
     * Get the offset for pagination
     */
    public int getOffset() {
        return page * size;
    }

    /**
     * Check if this query has any filters applied
     */
    public boolean hasFilters() {
        return (categories != null && !categories.isEmpty()) ||
                (retailers != null && !retailers.isEmpty()) ||
                minPrice != null || maxPrice != null ||
                retailerFilter != null || categoryFilter != null;
    }

    public enum SortOrder {
        ASC, DESC
    }
}