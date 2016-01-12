//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;
import rx.Observable;

public interface WebPageParser {
    /**
     * Parse given webPage and return all child pages
     * @param webPage Page to parse
     * @return All sub-pages
     * @throws Exception
     */
    Observable<WebPageEntity> parse(WebPageEntity webPage);

    /**
     * Can this class parse webPage?
     * @param webPage webPage to parse
     * @return True is this parser can parse this page, false otherwise
     */
    boolean canParse(WebPageEntity webPage);
}
