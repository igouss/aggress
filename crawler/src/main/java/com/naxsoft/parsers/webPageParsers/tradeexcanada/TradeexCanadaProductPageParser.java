package com.naxsoft.parsers.webPageParsers.tradeexcanada;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;


class TradeexCanadaProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeexCanadaProductPageParser.class);

    public TradeexCanadaProductPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        LOGGER.trace("Processing productPage {}", webPage.getUrl());
        if (webPage.getUrl().contains("out-stock") || webPage.getUrl().contains("-sold")) {
            return Observable.empty();
        } else {
            return PageDownloader.download(client, webPage, "productPageRaw")
                    .filter(data -> {
                        if (null != data) {
                            return true;
                        } else {
                            LOGGER.error("failed to download web page {}", webPage.getUrl());
                            return false;
                        }
                    }).doOnNext(e -> this.parseResultCounter.inc());
        }
    }

    @Override
    public String getParserType() {
        return "productPage";
    }

    @Override
    public String getSite() {
        return "tradeexcanada.com";
    }

}