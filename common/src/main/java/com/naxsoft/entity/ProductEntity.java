package com.naxsoft.entity;

/**
 *
 */
public class ProductEntity {
    /**
     *
     */
    private String json;

    /**
     *
     */
    private String url;

    public ProductEntity(String json, String url) {
        this.json = json;
        this.url = url;
    }

    public String getJson() {
        return this.json;
    }

    public String getUrl() {
        return url;
    }
}
