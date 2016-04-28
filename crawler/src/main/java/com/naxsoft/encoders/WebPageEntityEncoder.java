package com.naxsoft.encoders;

import com.naxsoft.entity.WebPageEntity;

/**
 *
 */
public class WebPageEntityEncoder extends Encoder<WebPageEntity> {
    /**
     * @param value
     * @return
     */
    public WebPageEntity decode(String value) {
        return fromJson(value, WebPageEntity.class);
    }
}
