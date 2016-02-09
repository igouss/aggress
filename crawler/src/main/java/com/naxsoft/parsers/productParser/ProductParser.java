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
     * @param webPage
     * @return
     * @throws Exception
     */
    Set<ProductEntity> parse(WebPageEntity webPage) throws Exception;

    /**
     * @param webPage
     * @return
     */
    boolean canParse(WebPageEntity webPage);
}
