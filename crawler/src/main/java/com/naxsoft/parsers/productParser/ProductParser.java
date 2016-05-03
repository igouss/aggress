//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;

import java.util.Set;

/**
 *
 */
public interface ProductParser {
    /**
     * @param webPage Page to parse
     * @return All products on the pahe
     * @throws Exception Parsing exeption
     */
    Set<ProductEntity> parse(WebPageEntity webPage) throws Exception;

    /**
     * @param webPage Can this parser process this page?
     * @return True if the parser can parse the page, false otherwise
     */
    boolean canParse(WebPageEntity webPage);
}
