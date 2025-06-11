package com.naxsoft.parsers.webPageParsers.psmilitaria;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;


class PsmilitariaProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(PsmilitariaProductListParser.class);

    public PsmilitariaProductListParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }


    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        WebPageEntity webPageEntity = new WebPageEntity(parent, parent.getContent(), "productPage", parent.getUrl(), parent.getCategory());
        return Observable.just(webPageEntity)
                .doOnNext(e -> this.parseResultCounter.inc());
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