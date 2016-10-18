package com.naxsoft.parsers.webPageParsers.canadiangunnutz;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
import org.asynchttpclient.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Copyright NAXSoft 2015
 */
class CanadiangunnutzProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadiangunnutzProductPageParser.class);
    private final HttpClient client;
    private final List<Cookie> cookies;

    private CanadiangunnutzProductPageParser(HttpClient client) {
        this.client = client;
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
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        LOGGER.trace("Processing productPage {}", webPage.getUrl());
        return PageDownloader.download(client, cookies, webPage, "productPageRaw");
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("canadiangunnutz.com") && webPage.getType().equals("productPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("canadiangunnutz.com/productPage", getParseRequestMessageHandler());
    }
}
