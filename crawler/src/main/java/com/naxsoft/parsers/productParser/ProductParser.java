package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;

import java.util.Collection;


/**
 * Copyright NAXSoft 2015
 */
interface ProductParser {
    /**
     * @param webPage Page to parse
     * @return All products on the pahe
     */
    Collection<ProductEntity> parse(WebPageEntity webPage);
}
