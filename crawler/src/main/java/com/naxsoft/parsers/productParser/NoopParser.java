package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;


/**
 * Copyright NAXSoft 2015
 */
class NoopParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoopParser.class);

    public NoopParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    @Override
    public Collection<ProductEntity> parse(WebPageEntity webPage) {
        LOGGER.warn("Why are we here?! page = " + webPage);
        return new HashSet<>(0);
    }

    @Override
    String getSite() {
        return "noopProductPageParser";
    }
}
