package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class NoopParser implements WebPageParser {
    @Override
    public Set<WebPageEntity> parse(String url) {
        Set<WebPageEntity> result = new HashSet<>();
        return result;
    }

    @Override
    public boolean canParse(String url, String action) {
        return false;
    }
}
