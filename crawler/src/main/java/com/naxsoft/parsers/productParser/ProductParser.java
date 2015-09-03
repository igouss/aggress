package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.Product;
import com.naxsoft.entity.WebPageEntity;

import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public interface ProductParser {
    Set<Product> parse(WebPageEntity url);
    boolean canParse(String url, String action);
}
