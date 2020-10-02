package com.naxsoft.parsers.webPageParsers.crafm;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
class CrafmProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrafmProductPageParser.class);
    private final List<Cookie> cookies;

    private CrafmProductPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
        cookies = new ArrayList<>(1);
        cookies.add(new DefaultCookie("store", "english"));
    }

    @Override
    public Iterable<WebPageEntity> parse(WebPageEntity webPage) {
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
        return "crafm.com";
    }

}
