package com.naxsoft.parsers.webPageParsers.canadiangunnutz;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
import org.asynchttpclient.cookie.Cookie;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class CanadiangunnutzFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadiangunnutzFrontPageParser.class);

    private static final Map<String, String> categories = new HashMap<>();

    static {
        categories.put("Precision and Target Rifles", "firearm");
        categories.put("Hunting and Sporting Arms", "firearm");
        categories.put("Military Surplus Rifle", "firearm");
        categories.put("Pistols and Revolvers", "firearm");
        categories.put("Shotguns", "firearm");
        categories.put("Modern Military and Black Rifles", "firearm");
        categories.put("Rimfire Firearms", "firearm");
        categories.put("Optics and Sights", "optic");
        categories.put("Factory Ammo and Reloading Equipment", "reload,ammo");
    }

    private final HttpClient client;
    private final Future<List<Cookie>> futureCookies;
    private List<Cookie> cookies = null;

    public CanadiangunnutzFrontPageParser(HttpClient client) {
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
            futureCookies = client.post("http://www.canadiangunnutz.com/forum/login.php?do=login", formParameters, new LinkedList<>(), getCookiesHandler());
        } catch (PropertyNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("h2.forumtitle > a");
            if (elements.isEmpty()) {
                LOGGER.error("No results on page");
            }

            for (Element element : elements) {
                String text = element.text();
                if (!text.startsWith("Exchange of")) {
                    continue;
                }
                for (String category : categories.keySet()) {
                    if (text.endsWith(category)) {
                        WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, element.attr("abs:href"), categories.get(category));
                        LOGGER.info("productList={}", webPageEntity.getUrl());
                        result.add(webPageEntity);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private Collection<WebPageEntity> parseDocument2(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Element element = document.select("#threadpagestats").first();
            String text = element.text();
            Matcher matcher = Pattern.compile("Threads (\\d+) to (\\d+) of (\\d+)").matcher(text);
            if (matcher.find()) {
                int postsPerPage = Integer.parseInt(matcher.group(2));
                int total = Integer.parseInt(matcher.group(3));
                int pages = (int) Math.ceil((double) total / postsPerPage);
                for (int i = 1; i <= pages; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, document.location() + "/page" + i, downloadResult.getSourcePage().getCategory());
                    LOGGER.info("productList={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
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
        WebPageEntity webPageEntity = new WebPageEntity(0L, "", "", false, "http://www.canadiangunnutz.com/forum/forum.php", "");
        return Observable.from(futureCookies, Schedulers.io())
                .map(cookies1 -> client.get(webPageEntity.getUrl(), cookies1, new DocumentCompletionHandler(webPageEntity)))
                .flatMap(Observable::from)
                .map(this::parseDocument)
                .flatMap(Observable::from)
                .map(webPageEntity1 -> client.get(webPageEntity1.getUrl(), cookies, new DocumentCompletionHandler(webPageEntity1)))
                .flatMap(Observable::from)
                .map(this::parseDocument2)
                .flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.canadiangunnutz.com/") && webPage.getType().equals("frontPage");
    }
}
