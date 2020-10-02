package com.naxsoft.parsers.webPageParsers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractWebPageParser implements WebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebPageParser.class);

    /**
     * @return website this parser can parse
     */
    protected abstract String getSite();

    /**
     * @return type of the page this parser can parse
     */
    protected abstract String getParserType();
}
