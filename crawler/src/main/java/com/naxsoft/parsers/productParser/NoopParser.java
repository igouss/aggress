//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.productParser.ProductParser;
import java.util.HashSet;
import java.util.Set;

public class NoopParser implements ProductParser {
    public NoopParser() {
    }

    public Set<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet result = new HashSet();
        return result;
    }

    public boolean canParse(String url, String action) {
        return false;
    }
}
