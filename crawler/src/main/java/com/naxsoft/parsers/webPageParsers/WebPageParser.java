package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.entity.WebPageEntity;

import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public interface WebPageParser {
    Set<WebPageEntity> parse(String url);
    boolean canParse(String url, String action);
}
