package com.naxsoft.parsers.webPageParsers.sail;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.DefaultCookie;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.asynchttpclient.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


class SailsProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SailsProductPageParser.class);
    private static final List<Cookie> cookies;

    static {
        cookies = new ArrayList<>(1);
        cookies.add(new DefaultCookie("store_language", "english"));
    }

    public SailsProductPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }


    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        LOGGER.trace("Processing productPage {}", webPage.getUrl());
        return PageDownloader.download(client, cookies, webPage, "productPageRaw")
                .filter(Objects::nonNull)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "productPage";
    }

    @Override
    public String getSite() {
        return "sail.ca";
    }

}