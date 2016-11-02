package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import io.reactivex.Flowable;


/**
 * Copyright NAXSoft 2015
 */
interface ProductParser {
    /**
     * @param webPage Page to parse
     * @return All products on the pahe
     */
    Flowable<ProductEntity> parse(WebPageEntity webPage);
}
