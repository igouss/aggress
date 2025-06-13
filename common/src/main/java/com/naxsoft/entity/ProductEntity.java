package com.naxsoft.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Immutable product entity representing a crawled product with all its metadata.
 * Uses Lombok for reduced boilerplate and improved maintainability.
 */
@Value
@Builder(toBuilder = true)
@Slf4j
@JsonDeserialize(builder = ProductEntity.ProductEntityBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductEntity {
    private static final Gson gson = new Gson();

    @NonNull
    String productName;

    @NonNull
    String url;

    String regularPrice;
    String specialPrice;
    String productImage;
    String description;

    @Builder.Default
    Instant modificationDate = Instant.now();

    @Builder.Default
    Map<String, String> attr = Collections.emptyMap();

    @Builder.Default
    List<String> category = Collections.emptyList();

    /**
     * Create a ProductEntity with basic information (backward compatibility)
     */
    public static ProductEntity create(String productName, String url, String regularPrice,
                                       String specialPrice, String productImage, String description,
                                       String... categories) {
        return ProductEntity.builder()
                .productName(productName)
                .url(url)
                .regularPrice(regularPrice)
                .specialPrice(specialPrice)
                .productImage(productImage)
                .description(description)
                .category(categories != null ? List.of(categories) : Collections.emptyList())
                .build();
    }

    /**
     * Legacy factory method for backward compatibility during Spring Boot migration.
     * TODO: Remove this method in Phase 2 when all legacy code is migrated.
     *
     * @param productName  Product name
     * @param url          Product URL
     * @param regularPrice Regular price
     * @param specialPrice Special/sale price
     * @param productImage Product image URL
     * @param description  Product description
     * @param attr         Product attributes map
     * @param category     Product categories array
     * @return ProductEntity instance
     */
    public static ProductEntity legacyCreate(String productName, String url, String regularPrice,
                                             String specialPrice, String productImage, String description,
                                             Map<String, String> attr, String[] category) {
        return ProductEntity.builder()
                .productName(productName)
                .url(url)
                .regularPrice(regularPrice)
                .specialPrice(specialPrice)
                .productImage(productImage)
                .description(description)
                .attr(attr != null ? attr : Collections.emptyMap())
                .category(category != null ? List.of(category) : Collections.emptyList())
                .build();
    }

    /**
     * Get JSON representation for Elasticsearch storage
     * @return json encoded product entity
     */
    public String getJson() {
        log.debug("Generating JSON for product: {}", productName);

        JsonObject jsonObject = new JsonObject();

        // Add non-null properties
        if (productName != null && !productName.isEmpty()) {
            jsonObject.addProperty("productName", productName);
        }
        if (url != null && !url.isEmpty()) {
            jsonObject.addProperty("url", url);
        }
        if (regularPrice != null && !regularPrice.isEmpty()) {
            jsonObject.addProperty("regularPrice", regularPrice);
        }
        if (specialPrice != null && !specialPrice.isEmpty()) {
            jsonObject.addProperty("specialPrice", specialPrice);
        }
        if (productImage != null && !productImage.isEmpty()) {
            jsonObject.addProperty("productImage", productImage);
        }
        if (description != null && !description.isEmpty()) {
            jsonObject.addProperty("description", description);
        }

        jsonObject.addProperty("modificationDate", modificationDate.toString());

        // Add attributes
        attr.forEach(jsonObject::addProperty);

        // Add categories
        if (category != null && !category.isEmpty()) {
            JsonArray categoryArray = new JsonArray();
            category.forEach(categoryArray::add);
            jsonObject.add("category", categoryArray);
        }

        return gson.toJson(jsonObject);
    }

    /**
     * Check if the product has a discount (special price different from regular price)
     * @return true if product has a discount
     */
    public boolean hasDiscount() {
        return specialPrice != null && !specialPrice.equals(regularPrice);
    }

    /**
     * Log product processing information
     */
    public void logProcessing() {
        log.info("Processing product: {} from URL: {}", productName, url);
    }
}
