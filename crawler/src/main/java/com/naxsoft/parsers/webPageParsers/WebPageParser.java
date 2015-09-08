//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;
import java.util.Set;

public interface WebPageParser {
    Set<WebPageEntity> parse(String var1) throws Exception;

    boolean canParse(String var1, String var2);
}
