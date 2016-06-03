package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
class NoopParser extends AbstractRawPageParser {
    /**
     *
     */
    NoopParser() {
    }

    /**
     * @param webPageEntity
     * @return
     */
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) {
        return new HashSet<>();
    }

    /**
     * @param webPage
     * @return
     */
    public boolean canParse(WebPageEntity webPage) {
        return false;
    }
}
