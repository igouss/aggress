//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import java.util.HashSet;
import java.util.Set;

public class NoopParser implements WebPageParser {
    public NoopParser() {
    }

    public Set<WebPageEntity> parse(String url) {
        HashSet result = new HashSet();
        return result;
    }

    public boolean canParse(String url, String action) {
        return false;
    }
}
