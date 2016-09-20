package com.naxsoft.encoders;


import com.naxsoft.entity.ProductEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductEntityEncoder extends Encoder {
    private final static Logger LOGGER = LoggerFactory.getLogger(ProductEntityEncoder.class);

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
            LOGGER.error("Failed to parse" + value);
            return null;
        }
    }
}
