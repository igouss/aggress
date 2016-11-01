package com.naxsoft.parsers.webPageParsers.psmilitaria;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * Copyright NAXSoft 2015
 */
class PsmilitariaProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(PsmilitariaProductListParser.class);

    private PsmilitariaProductListParser(HttpClient client) {

    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        WebPageEntity webPageEntity = new WebPageEntity(parent, parent.getContent(), "productPage", parent.getUrl(), parent.getCategory());
        return Observable.just(webPageEntity);
    }

    @Override
    public String getParserType() {
        return "productList";
    }

    @Override
    public String getSite() {
        return "psmilitaria.50megs.com";
    }

}