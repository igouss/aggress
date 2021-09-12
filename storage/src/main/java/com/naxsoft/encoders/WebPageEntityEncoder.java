package com.naxsoft.encoders;

import com.naxsoft.common.entity.WebPageEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebPageEntityEncoder extends Encoder {
    /**
     * Deserialize WebPageEntity from json into Java object
     *
     * @param value Serialized value
     * @return Java object
     */
    public static WebPageEntity decode(String value) {
        try {
            return fromJson(value, WebPageEntity.class);
        } catch (RuntimeException e) {
            log.error("Failed to parse" + value);
            return null;
        }
    }
}
