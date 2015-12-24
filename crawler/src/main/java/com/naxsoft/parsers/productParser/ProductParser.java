//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;

import java.util.Set;

public interface ProductParser {
    Set<ProductEntity> parse(WebPageEntity webPage) throws Exception;

    boolean canParse(WebPageEntity webPage);
}
