package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;
import rx.Observable;

/**
 * Copyright NAXSoft 2015
 */
public interface WebPageParser {
    /**
     * Parse given webPage and return all child pages
     *
     * @param webPage Page to parse
     * @return All sub-pages
     */
    Observable<WebPageEntity> parse(WebPageEntity webPage);

    /**
     * Can this class parse webPage?
     *
     * @param webPage webPage to parse
     * @return True is this parser can parse this page, false otherwise
     */
    boolean canParse(WebPageEntity webPage);
}
