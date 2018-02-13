package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;
import rx.Observable;

public interface WebPageParser {
    /**
     * Parse given webPage and return all child pages
     *
     * @param webPage Page to parse
     * @return All sub-pages
     */
    Observable<WebPageEntity> parse(WebPageEntity webPage);
}
