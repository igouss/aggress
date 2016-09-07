package com.naxsoft.encoders;


import com.naxsoft.entity.ProductEntity;

public class ProductEntityEncoder extends Encoder {
    /**
     * Deserialize ProductEntity from json into Java object
     *
     * @param value Serialized value
     * @return Java object
     */
    public static ProductEntity decode(String value) {
        return fromJson(value, ProductEntity.class);
    }
}
