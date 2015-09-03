package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.Product;
import com.naxsoft.entity.WebPageEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class NoopParser implements ProductParser {
    @Override
    public Set<Product> parse(WebPageEntity webPageEntity) {
        Set<Product> result = new HashSet<>();
        return result;
    }

    @Override
    public boolean canParse(String url, String action) {
        return false;
    }
}
