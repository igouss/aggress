package com.naxsoft.parsers.webPageParsers.canadiangunnutz;

import com.naxsoft.crawler.HttpClient;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadiangunnutzProductPageParser.class);
    private final HttpClient client;
    private final ListenableFuture<List<Cookie>> futureCookies;

    public CanadiangunnutzProductPageParser(HttpClient client) {
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

        futureCookies = client.post("http://www.canadiangunnutz.com/forum/login.php?do=login", formParameters, new LinkedHashSet<>(), getCookiesHandler());
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return Observable.create(subscriber -> {
            try {
                List<Cookie> cookies = futureCookies.get();
                if (null == cookies || cookies.isEmpty()) {
                    LOGGER.warn("No login cookies");
                    subscriber.onCompleted();
                    return;
                }

                Observable.from(PageDownloader.download(client, cookies, webPage.getUrl()))
                        .filter(data -> {
                            if (null != data) {
                                return true;
                            } else {
                                LOGGER.error("failed to download web page {}", webPage.getUrl());
                                return false;
                            }
                        })
                        .map(webPageEntity -> {
                            webPageEntity.setCategory(webPage.getCategory());
                            return webPageEntity;
                        }).subscribe(subscriber::onNext, subscriber::onError, subscriber::onCompleted);
            } catch (Exception e) {
                LOGGER.error("Failed to login to canadiangunnutz", e);
            }
        });

    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.canadiangunnutz.com/") && webPage.getType().equals("productPage");
    }
}
