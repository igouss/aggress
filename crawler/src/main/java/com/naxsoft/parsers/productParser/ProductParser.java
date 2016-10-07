package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import rx.Observable;

/**
 * Copyright NAXSoft 2015
 */
interface ProductParser {
    /**
     * @param webPage Page to parse
     * @return All products on the pahe
     */
    Observable<ProductEntity> parse(WebPageEntity webPage);

    /**
     * @param webPage Can this parser process this page?
     * @return True if the parser can parse the page, false otherwise
     */
    boolean canParse(WebPageEntity webPage);
}
