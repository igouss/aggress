package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import reactor.core.publisher.Flux;


interface ProductParser {
    /**
     * @param webPage Page to parse
     * @return All products on the pahe
     */
    Flux<ProductEntity> parse(WebPageEntity webPage);
}
