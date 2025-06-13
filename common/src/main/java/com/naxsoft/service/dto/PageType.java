package com.naxsoft.service.dto;

import lombok.Value;

/**
 * Immutable page type definitions for the crawler system.
 * Uses Lombok @Value for type safety and immutability.
 */
@Value
public class PageType {

    public static final PageType FRONT_PAGE = PageType.of("frontPage");
    public static final PageType PRODUCT_LIST = PageType.of("productList");
    public static final PageType PRODUCT_PAGE = PageType.of("productPage");
    public static final PageType PRODUCT_PAGE_RAW = PageType.of("productPageRaw");

    String value;

    /**
     * Create a PageType instance
     *
     * @param value the page type value
     * @return new PageType instance
     */
    public static PageType of(String value) {
        return new PageType(value);
    }

    /**
     * Check if this is a product-related page type
     *
     * @return true if this page type relates to products
     */
    public boolean isProductPage() {
        return PRODUCT_PAGE.equals(this) || PRODUCT_PAGE_RAW.equals(this);
    }

    /**
     * Check if this is a listing page type
     *
     * @return true if this page type lists multiple items
     */
    public boolean isListingPage() {
        return PRODUCT_LIST.equals(this) || FRONT_PAGE.equals(this);
    }
}