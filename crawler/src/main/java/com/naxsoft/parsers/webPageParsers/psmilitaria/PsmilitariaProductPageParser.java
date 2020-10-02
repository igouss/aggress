package com.naxsoft.parsers.webPageParsers.psmilitaria;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
class PsmilitariaProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(PsmilitariaProductPageParser.class);

    @Override
    public Iterable<WebPageEntity> parse(WebPageEntity webPage) {
        LOGGER.trace("Processing productPage {}", webPage.getUrl());
        return PageDownloader.download(client, webPage, "productPageRaw")
                .filter(data -> null != data)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "productPage";
    }

    @Override
    public String getSite() {
        return "psmilitaria.50megs.com";
    }

}