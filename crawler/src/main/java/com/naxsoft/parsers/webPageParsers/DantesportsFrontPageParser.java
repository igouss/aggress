package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;

import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class DantesportsFrontPageParser implements WebPageParser {
    @Override
    public Set<WebPageEntity> parse(WebPageEntity webPage) throws Exception {
        return null;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return false;
    }
}
