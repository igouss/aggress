package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import io.reactivex.Observable;


/**
 * Copyright NAXSoft 2015
 */
interface ProductParser {
    /**
     * @param webPage Page to parse
     * @return All products on the pahe
     */
    Observable<ProductEntity> parse(WebPageEntity webPage);
}
