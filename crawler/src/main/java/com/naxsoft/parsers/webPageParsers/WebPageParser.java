package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;
import reactor.core.publisher.Flux;


public interface WebPageParser {
    /**
     * Parse given webPage and return all child pages
     *
     * @param webPage Page to parse
     * @return All sub-pages
     */
    Flux<WebPageEntity> parse(WebPageEntity webPage);
}
