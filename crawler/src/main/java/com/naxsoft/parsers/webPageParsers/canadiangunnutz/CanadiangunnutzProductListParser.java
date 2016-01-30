package com.naxsoft.parsers.webPageParsers.canadiangunnutz;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.utils.AppProperties;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Copyright NAXSoft 2015
 */
public class CanadiangunnutzProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadiangunnutzProductListParser.class);
    private final HttpClient client;
    private final ListenableFuture<List<Cookie>> futureCookies;

    private Collection<WebPageEntity> parseDocument(Document document) {
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
                        WebPageEntity webPageEntity = new WebPageEntity();
                        webPageEntity.setUrl(element.attr("abs:href"));
                        webPageEntity.setParsed(false);
                        webPageEntity.setType("productPage");
                        LOGGER.info("productPage={}", webPageEntity.getUrl());
                        result.add(webPageEntity);
                    }
                }
            }
        }
        return result;
    }

    public CanadiangunnutzProductListParser(HttpClient client) {
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
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        try {
            List<Cookie> cookies = futureCookies.get();
            return Observable.from(client.get(parent.getUrl(), cookies, new DocumentCompletionHandler()))
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
