package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

class NoopParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoopParser.class);

    public NoopParser(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPage) {
        Set<ProductEntity> result = new HashSet<>(0);
        LOGGER.warn("Why are we here?! page = " + webPage);
        return result;
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
