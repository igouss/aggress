package com.naxsoft.crawler.parsers.parsers.productParser;

import com.naxsoft.common.entity.ProductEntity;
import com.naxsoft.common.entity.WebPageEntity;

import java.util.Set;

interface ProductParser {
    /**
     * @param webPage Page to parse
     * @return All products on the path
     */
    Set<ProductEntity> parse(WebPageEntity webPage);
}
