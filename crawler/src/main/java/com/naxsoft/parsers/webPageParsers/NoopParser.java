package com.naxsoft.parsers.webPageParsers;

import java.util.Set;
import com.naxsoft.entity.WebPageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Copyright NAXSoft 2015
 */
class NoopParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoopParser.class);

    @Override
    public Iterable<WebPageEntity> parse(WebPageEntity webPage) {
        LOGGER.error("Using NOOP parser for: " + webPage);
        return Set.of();
    }

    @Override
    public String getParserType() {
        return "noopWebPageParser";
    }

    @Override
    public String getSite() {
        return "anySite";
    }
}
