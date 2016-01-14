package com.naxsoft.parsers.webPageParsers.canadiangunnutz;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import com.naxsoft.utils.AppProperties;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Copyright NAXSoft 2015
 */
public class CanadiangunnutzProductPageParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(CanadiangunnutzProductPageParser.class);
    private final AsyncFetchClient client;
    private List<Cookie> cookies;

    public CanadiangunnutzProductPageParser(AsyncFetchClient client) {
        this.client = client;
        Map<String, String> formParameters = new HashMap<>();
        formParameters.put("vb_login_username", AppProperties.getProperty("canadiangunnutzLogin"));
        formParameters.put("vb_login_password", AppProperties.getProperty("canadiangunnutzPassword"));
        formParameters.put("vb_login_password_hint", "Password");
        formParameters.put("s", "");
        formParameters.put("securitytoken", "guest");
        formParameters.put("do", "login");
        formParameters.put("vb_login_md5password", "");
        formParameters.put("vb_login_md5password_utf", "");

        ListenableFuture<List<Cookie>> futureCookies = client.post("http://www.canadiangunnutz.com/forum/login.php?do=login", formParameters, new LinkedHashSet<>(), getCookiesHandler());
        try {
            cookies = futureCookies.get();
        } catch (Exception e) {
            logger.error("Failed to login to canadiangunnutz", e);
        }
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.from(PageDownloader.download(client, cookies, webPage.getUrl()))
                .filter(data -> {
                    if (null != data) {
                        return true;
                    } else {
                        logger.error("failed to download web page {}" + webPage.getUrl());
                        return false;
                    }
                })
                .map(webPageEntity -> {
                    webPageEntity.setCategory(webPage.getCategory());
                    return webPageEntity;
                });
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.canadiangunnutz.com/") && webPage.getType().equals("productPage");
    }
}
