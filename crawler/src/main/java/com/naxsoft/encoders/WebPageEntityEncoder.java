package com.naxsoft.encoders;

import com.naxsoft.entity.WebPageEntity;

/**
 *
 */
public class WebPageEntityEncoder extends Encoder {
    /**
     * Deserialize WebPageEntity from json into Java object
     *
     * @param value Serialized value
     * @return Java object
     */
    public static WebPageEntity decode(String value) {
        return fromJson(value, WebPageEntity.class);
    }
}
