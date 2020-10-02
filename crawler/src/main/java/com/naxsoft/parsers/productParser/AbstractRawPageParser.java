package com.naxsoft.parsers.productParser;

import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
abstract class AbstractRawPageParser extends AbstractVerticle implements ProductParser {
    private static final Logger LOGGER = LoggerFactory.getLogger("RawPageParser");

    /**
     * @return website this parser can parse
     */
    abstract String getSite();

    /**
     * @return type of the page this parser can parse
     */
    abstract String getParserType();
}
