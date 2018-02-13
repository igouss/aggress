package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import rx.Observable;

interface ProductParser {
    /**
     * @param webPage Page to parse
     * @return All products on the path
     */
    Observable<ProductEntity> parse(WebPageEntity webPage);
}
