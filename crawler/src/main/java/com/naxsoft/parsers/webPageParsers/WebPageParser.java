package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;
import io.reactivex.Flowable;


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
    Flowable<WebPageEntity> parse(WebPageEntity webPage);
}
