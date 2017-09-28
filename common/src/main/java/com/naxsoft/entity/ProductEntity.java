package com.naxsoft.entity;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

/**
 *
 */
public class ProductEntity {
    private final String productName;
    private final String url;
    private final String regularPrice;
    private final String specialPrice;
    private final String productImage;
    private final String description;
    private final Map<String, String> attr;
    private final String[] category;
    private final Instant modificationDate;

    /**
     * @param productName
     * @param url
     * @param regularPrice
     * @param specialPrice
     * @param productImage
     * @param description
     * @param categories
     */
    public ProductEntity(String productName, String url, String regularPrice, String specialPrice, String productImage, String description, String... categories) {
        this(productName, url, regularPrice, specialPrice, productImage, description, Collections.emptyMap(), categories);
    }

    /**
     * @param productName
     * @param url
     * @param regularPrice
     * @param specialPrice
     * @param productImage
     * @param description
     * @param attr
     * @param category
     */
    public ProductEntity(String productName, String url, String regularPrice, String specialPrice, String productImage, String description, Map<String, String> attr, String... category) {
        this.productName = productName;
        this.category = category;
        this.url = url;
        this.modificationDate = Instant.now();
        this.regularPrice = regularPrice;
        this.specialPrice = specialPrice;
        this.productImage = productImage;
        this.description = description;
        this.attr = attr;


    }

    public String getUrl() {
        return url;
    }

    public String getRegularPrice() {
        return regularPrice;
    }

    public String getSpecialPrice() {
        return specialPrice;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getAttr() {
        return attr;
    }

    public String[] getCategory() {
        return category;
    }

    public Instant getModificationDate() {
        return modificationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductEntity that = (ProductEntity) o;

        return productName.equals(that.productName) && url.equals(that.url);
    }

    @Override
    public int hashCode() {
        int result = productName.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ProductEntity{" +
                "url='" + url + '\'' +
                '}';
    }
}
