package com.naxsoft.encoders;


import com.naxsoft.common.entity.ProductEntity;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ProductEntityEncoder extends Encoder {
    /**
     * Deserialize ProductEntity from json into Java object
     *
     * @param value Serialized value
     * @return Java object
     */
    public static ProductEntity decode(String value) {
        try {
            return fromJson(value, ProductEntity.class);
        } catch (Exception e) {
            log.error("Failed to parse" + value);
            return null;
        }
    }
}
