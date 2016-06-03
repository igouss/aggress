package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;

import java.util.Collections;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
class NoopParser extends AbstractRawPageParser {
    @Override
    public Set<ProductEntity> parse(WebPageEntity webPage) throws ProductParseException {
        return Collections.emptySet();
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return true;
    }
}
