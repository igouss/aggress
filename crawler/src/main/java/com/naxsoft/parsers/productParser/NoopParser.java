package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * Copyright NAXSoft 2015
 */
class NoopParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoopParser.class);

    @Override
    public Observable<ProductEntity> parse(WebPageEntity webPage) {
        LOGGER.warn("Why are we here?! page = " + webPage);
        return Observable.empty();
    }

    @Override
    String getSite() {
        return "noopProductPageParser";
    }

    @Override
    String getType() {
        return "productPageRaw";
    }

}
