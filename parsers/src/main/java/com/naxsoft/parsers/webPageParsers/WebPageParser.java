package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;

import java.util.List;

public interface WebPageParser {
    /**
     * Parse given webPage and return all child pages
     *
     * @param webPage Page to parse
     * @return All sub-pages
     */
    List<WebPageEntity> parse(WebPageEntity webPage);
}
