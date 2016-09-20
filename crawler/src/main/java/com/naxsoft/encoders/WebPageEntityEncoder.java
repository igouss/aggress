package com.naxsoft.encoders;

import com.naxsoft.entity.WebPageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class WebPageEntityEncoder extends Encoder {
    private final static Logger LOGGER = LoggerFactory.getLogger(WebPageEntityEncoder.class);

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
            LOGGER.error("Failed to parse" + value);
            return null;
        }
    }
}
