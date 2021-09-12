package com.naxsoft.crawler.parsers.parsers.productParser;

import com.naxsoft.common.entity.ProductEntity;
import com.naxsoft.common.entity.WebPageEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Slf4j
class NoopParser extends AbstractRawPageParser {
    @Override
    public Set<ProductEntity> parse(WebPageEntity webPage) {
        Set<ProductEntity> result = new HashSet<>(0);
        log.warn("Why are we here?! page = " + webPage);
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
