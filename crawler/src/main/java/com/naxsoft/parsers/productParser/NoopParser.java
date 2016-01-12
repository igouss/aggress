//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;

import java.util.HashSet;
import java.util.Set;

public class NoopParser extends AbstractRawPageParser {
    public NoopParser() {
    }

    public Set<ProductEntity> parse(WebPageEntity webPageEntity) {
        return new HashSet<>();
    }

    public boolean canParse(WebPageEntity webPage) {
        return false;
    }
}
