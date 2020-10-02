package com.naxsoft.parsers.webPageParsers.canadiangunnutz;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
import io.netty.handler.codec.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Copyright NAXSoft 2015
 */
class CanadiangunnutzProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadiangunnutzProductPageParser.class);
    private final List<Cookie> cookies;

    private CanadiangunnutzProductPageParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
        Map<String, String> formParameters = new HashMap<>();
        try {
            formParameters.put("vb_login_username", AppProperties.getProperty("canadiangunnutzLogin"));
            formParameters.put("vb_login_password", AppProperties.getProperty("canadiangunnutzPassword"));
            formParameters.put("vb_login_password_hint", "Password");
            formParameters.put("s", "");
            formParameters.put("securitytoken", "guest");
            formParameters.put("do", "login");
            formParameters.put("vb_login_md5password", "");
            formParameters.put("vb_login_md5password_utf", "");
            cookies = client.post("http://www.canadiangunnutz.com/forum/login.php?do=login", formParameters, new LinkedList<>(), getCookiesHandler()).toBlocking().first();
        } catch (PropertyNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Iterable<WebPageEntity> parse(WebPageEntity webPage) {
        LOGGER.trace("Processing productPage {}", webPage.getUrl());
        return PageDownloader.download(client, cookies, webPage, "productPageRaw")
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "productPage";
    }

    @Override
    public String getSite() {
        return "canadiangunnutz.com";
    }

}
