package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;

import java.util.Set;

interface ProductParser {
    /**
     * @param webPage Page to parse
     * @return All products on the path
     */
    Set<ProductEntity> parse(WebPageEntity webPage);
}
