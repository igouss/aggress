package com.naxsoft.encoders;


import com.google.gson.Gson;

public class Encoder {
    private static final Gson gson = new Gson();

    /**
     * Convert from JSON representation
     *
     * @param json ProductEntity in JSON format
     * @return ProductEntity object
     */
    static <T> T fromJson(String json, Class<T> clazzOfT) {
        return gson.fromJson(json, clazzOfT);
    }

    /**
     * Get JSON representation
     *
     * @return JSON representation
     */
    public static <T> String encode(T entity) {
        return gson.toJson(entity);
    }
}
