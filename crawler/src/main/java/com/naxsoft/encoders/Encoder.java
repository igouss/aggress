package com.naxsoft.encoders;


import com.google.gson.Gson;

public class Encoder {
    /**
     * Convert from JSON representation
     *
     * @param json ProductEntity in JSON format
     * @return ProductEntity object
     */
    static <T> T fromJson(String json, Class<T> clazzOfT) {
        Gson gson = new Gson();
        return gson.fromJson(json, clazzOfT);
    }

    /**
     * Get JSON representation
     *
     * @return JSON representation
     */
    public <T> String encode(T entity) {
        Gson gson = new Gson();
        return gson.toJson(entity);
    }
}
