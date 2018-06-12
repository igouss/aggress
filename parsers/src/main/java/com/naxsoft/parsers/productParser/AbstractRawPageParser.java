package com.naxsoft.parsers.productParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractRawPageParser implements ProductParser {
    private static final Logger LOGGER = LoggerFactory.getLogger("RawPageParser");

    public AbstractRawPageParser() {
    }

    /**
     * @return website this parser can parse
     */
    abstract String getSite();

    /**
     * @return type of the page this parser can parse
     */
    abstract String getParserType();
}
