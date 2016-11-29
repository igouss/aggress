package com.naxsoft.entity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.Map;

/**
 *
 */
public class ProductEntity {
    private final static Gson gson = new Gson();
    private final String productName;
    private final String url;
    private final String regularPrice;
    private final String specialPrice;
    private final String json;

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
        this.url = url;
        Timestamp modificationDate = new Timestamp(System.currentTimeMillis());
        this.regularPrice = regularPrice;
        this.specialPrice = specialPrice;

        JsonObject jsonObject = new JsonObject();
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

        attr.forEach(jsonObject::addProperty);

        if (category != null && category.length > 0) {
            JsonArray categoryArray = new JsonArray();
            for (String cat : category) {
                categoryArray.add(cat);
            }

            if (categoryArray.size() != 0) {
                jsonObject.add("category", categoryArray);
            }
        }
        json = gson.toJson(jsonObject);
    }

    /**
     * Get JSON representation
     *
     * @return json encoded product entity
     */
    public String getJson() {
        return json;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProductEntity that = (ProductEntity) o;

        return getJson().equals(that.getJson()) && url.equals(that.url);
    }


    @Override
    public int hashCode() {
        int result = getJson().hashCode();
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
