//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;
import rx.Observable;

import java.util.Set;

public interface WebPageParser {
    Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) throws Exception;

    boolean canParse(WebPageEntity webPage);
}
