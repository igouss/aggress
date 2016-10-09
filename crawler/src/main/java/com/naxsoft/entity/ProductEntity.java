package com.naxsoft.entity;

/**
 *
 */
public class ProductEntity {
    /**
     *
     */
    private final String json;

    /**
     *
     */
    private final String url;

    public ProductEntity(String json, String url) {
        this.json = json;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductEntity that = (ProductEntity) o;

        return json.equals(that.json) && url.equals(that.url);

    }

    @Override
    public int hashCode() {
        int result = json.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ProductEntity{" +
                "json='" + json + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
