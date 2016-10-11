package com.naxsoft.parsers.webPageParsers.canadiangunnutz;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import org.asynchttpclient.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;

/**
 * Copyright NAXSoft 2015
 */
class CanadiangunnutzProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadiangunnutzProductPageParser.class);
    private final HttpClient client;
    private final Observable<List<Cookie>> futureCookies;

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
            futureCookies = client.post("http://www.canadiangunnutz.com/forum/login.php?do=login", formParameters, new LinkedHashSet<>(), getCookiesHandler());
        } catch (PropertyNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return futureCookies.flatMap(cookies -> PageDownloader.download(client, cookies, webPage, "productPageRaw"));
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("canadiangunnutz.com") && webPage.getType().equals("productPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("canadiangunnutz.com/productPage", (Message<WebPageEntity> event) -> {
            vertx.executeBlocking(future -> {
                Iterator<WebPageEntity> iterator = parse(event.body()).toBlocking().getIterator();
                future.complete(iterator);
            }, (AsyncResult<Iterator<WebPageEntity>> result) -> {
                if (result.succeeded()) {
                    Iterator<WebPageEntity> it = result.result();
                    while (it.hasNext()) {
                        vertx.eventBus().publish("webPageParseResult", it.next());
                    }
                } else {
                    LOGGER.error("Failed to parse", result.cause());
                }
            });
        });
    }
}
