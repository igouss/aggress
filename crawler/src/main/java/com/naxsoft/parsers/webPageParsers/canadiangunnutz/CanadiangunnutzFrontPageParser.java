package com.naxsoft.parsers.webPageParsers.canadiangunnutz;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.utils.AppProperties;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.cookie.Cookie;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class CanadiangunnutzFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadiangunnutzFrontPageParser.class);
    private static final String[] CATEGORIES = {
            "Precision and Target Rifles",
            "Hunting and Sporting Arms",
            "Military Surplus Rifle",
            "Pistols and Revolvers",
            "Shotguns",
            "Modern Military and Black Rifles",
            "Rimfire Firearms",
            "Optics and Sights",
            "Factory Ammo and Reloading Equipment",
    };
    private final HttpClient client;
    private final ListenableFuture<List<Cookie>> futureCookies;
    private List<Cookie> cookies = null;

    public CanadiangunnutzFrontPageParser(HttpClient client) {
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

        futureCookies = client.post("http://www.canadiangunnutz.com/forum/login.php?do=login", formParameters, new LinkedList<>(), getCookiesHandler());
    }

    private Collection<WebPageEntity> parseDocument(Document document) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Elements elements = document.select("h2.forumtitle > a");
        if (elements.isEmpty()) {
            LOGGER.error("No results on page");
        }

        for (Element element : elements) {
            String text = element.text();
            if (!text.startsWith("Exchange of")) {
                continue;
            }
            for (String category : CATEGORIES) {
                if (text.endsWith(category)) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(element.attr("abs:href"));
                    webPageEntity.setParsed(false);
                    webPageEntity.setType("productList");
                    webPageEntity.setCategory("n/a");
                    LOGGER.info("productList={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                    break;
                }
            }
        }
        return result;
    }

    private Collection<WebPageEntity> parseDocument2(Document document) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Element element = document.select("#threadpagestats").first();
        String text = element.text();
        Matcher matcher = Pattern.compile("Threads (\\d+) to (\\d+) of (\\d+)").matcher(text);
        if (matcher.find()) {
            int postsPerPage = Integer.parseInt(matcher.group(2));
            int total = Integer.parseInt(matcher.group(3));
            int pages = (int) Math.ceil((double) total / postsPerPage);
            for (int i = 1; i <= pages; i++) {
                WebPageEntity webPageEntity = new WebPageEntity();
                webPageEntity.setUrl(document.location() + "/page" + i);
                webPageEntity.setParsed(false);
                webPageEntity.setType("productList");
                webPageEntity.setCategory("n/a");
                LOGGER.info("productList={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        try {
            cookies = futureCookies.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return Observable.from(futureCookies)
                .map(cookies1 -> client.get("http://www.canadiangunnutz.com/forum/forum.php", cookies1, new DocumentCompletionHandler()))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from)
                .map(webPageEntity -> client.get(webPageEntity.getUrl(), cookies, new DocumentCompletionHandler()))
                .flatMap(Observable::from)
                .map(this::parseDocument2)
                .flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.canadiangunnutz.com/") && webPage.getType().equals("frontPage");
    }
}
