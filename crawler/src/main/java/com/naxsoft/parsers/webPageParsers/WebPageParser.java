package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;

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
    Iterable<WebPageEntity> parse(WebPageEntity webPage) throws Exception;
}
