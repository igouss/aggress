package com.naxsoft.parsers.webPageParsers.canadiangunnutz;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
import com.ning.http.client.cookie.Cookie;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
class CanadiangunnutzProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadiangunnutzProductListParser.class);
    private final HttpClient client;
    private final Future<List<Cookie>> futureCookies;

    public CanadiangunnutzProductListParser(HttpClient client) {
        this.client = client;
        Map<String, String> formParameters = new HashMap<>();
        try {
            formParameters.put("vb_login_username", AppProperties.getProperty("canadiangunnutzLogin").getValue());
            formParameters.put("vb_login_password", AppProperties.getProperty("canadiangunnutzPassword").getValue());
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

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Document document = downloadResult.getDocument();

        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select("#threads .threadtitle");
        if (elements.isEmpty()) {
            LOGGER.error("No results on page");
        }
        for (Element element : elements) {
            Elements select = element.select(".prefix");
            if (!select.isEmpty()) {
                if (select.first().text().contains("WTS")) {
                    element = element.select("a.title").first();
                    if (!element.text().toLowerCase().contains("remove")) {
                        WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productPage", false, element.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                        LOGGER.info("productPage={}", webPageEntity.getUrl());
                        result.add(webPageEntity);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        try {
            List<Cookie> cookies = futureCookies.get();
            return Observable.from(client.get(parent.getUrl(), cookies, new DocumentCompletionHandler(parent)), Schedulers.io())
                    .map(this::parseDocument)
                    .flatMap(Observable::from);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return Observable.empty();
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.canadiangunnutz.com/") && webPage.getType().equals("productList");
    }
}
