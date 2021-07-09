package com.naxsoft.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.naxsoft.entity.ProductEntity;

import java.time.format.DateTimeFormatter;

public class JsonEncoder {
    private final static Gson gson = new Gson();
    private final static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_INSTANT;

    /**
     * Get JSON representation
     *
     * @return json encoded product entity
     */
    public static String toJson(ProductEntity productEntity) {
        JsonObject jsonObject = new JsonObject();
        if (productEntity.getProductName() != null && !productEntity.getProductName().isEmpty()) {
            jsonObject.addProperty("productName", productEntity.getProductName());
        }
        if (productEntity.getUrl() != null && !productEntity.getUrl().isEmpty()) {
            jsonObject.addProperty("url", productEntity.getUrl());
        }
        if (productEntity.getRegularPrice() != null && !productEntity.getRegularPrice().isEmpty()) {
            jsonObject.addProperty("regularPrice", productEntity.getRegularPrice());
        }
        if (productEntity.getSpecialPrice() != null && !productEntity.getSpecialPrice().isEmpty()) {
            jsonObject.addProperty("specialPrice", productEntity.getSpecialPrice());
        }
        if (productEntity.getProductImage() != null && !productEntity.getProductImage().isEmpty()) {
            jsonObject.addProperty("productImage", productEntity.getProductImage());
        }
        if (productEntity.getDescription() != null && !productEntity.getDescription().isEmpty()) {
            jsonObject.addProperty("description", productEntity.getDescription());
        }

        productEntity.getAttr().forEach(jsonObject::addProperty);

        if (productEntity.getCategory() != null && productEntity.getCategory().length > 0) {
            JsonArray categoryArray = new JsonArray();
            for (String cat : productEntity.getCategory()) {
                categoryArray.add(cat);
            }

            if (categoryArray.size() != 0) {
                jsonObject.add("category", categoryArray);
            }
        }
        return gson.toJson(jsonObject);
    }
}
