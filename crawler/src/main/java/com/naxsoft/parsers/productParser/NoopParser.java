package com.naxsoft.parsers.productParser;

import java.util.Set;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
class NoopParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoopParser.class);

    @Override
    public Iterable<ProductEntity> parse(WebPageEntity webPage) {
        LOGGER.error("Why are we here?! page = " + webPage);
        return Set.of();
    }

    @Override
    String getSite() {
        return "noopProductPageParser";
    }

    @Override
    String getParserType() {
        return "productPageRaw";
    }

}
