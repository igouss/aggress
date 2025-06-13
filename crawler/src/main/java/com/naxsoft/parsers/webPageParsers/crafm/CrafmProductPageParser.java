package com.naxsoft.parsers.webPageParsers.crafm;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.DefaultCookie;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.asynchttpclient.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;


class CrafmProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrafmProductPageParser.class);
    private final List<Cookie> cookies;

    private CrafmProductPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
        cookies = new ArrayList<>(1);
        cookies.add(new DefaultCookie("store", "english"));
    }

    @Override
    public Flux<WebPageEntity> parse(WebPageEntity webPage) {
        LOGGER.trace("Processing productPage {}", webPage.getUrl());
        return PageDownloader.download(client, cookies, webPage, "productPageRaw")
                .filter(data -> null != data)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "productPage";
    }

    @Override
    public String getSite() {
        return "crafm.com";
    }


}
