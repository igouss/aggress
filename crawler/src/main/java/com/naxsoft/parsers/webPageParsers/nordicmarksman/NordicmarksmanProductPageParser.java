package com.naxsoft.parsers.webPageParsers.nordicmarksman;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import io.reactivex.Observable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NordicmarksmanProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(NordicmarksmanProductPageParser.class);

    public NordicmarksmanProductPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
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
        return "nordicmarksman.com";
    }

}