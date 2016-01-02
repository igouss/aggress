package com.naxsoft.parsers.webPageParsers.canadiangunnutz;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.naxsoft.utils.AppProperties;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import com.ning.http.client.cookie.Cookie;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class CanadiangunnutzFrontPageParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(CanadiangunnutzFrontPageParser.class);
    private static final String[] categories = {
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
    private final AsyncFetchClient client;

    public CanadiangunnutzFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    private static AsyncCompletionHandler<List<Cookie>> getEngCookiesHandler() {
        return new AsyncCompletionHandler<List<Cookie>>() {
            @Override
            public List<Cookie> onCompleted(com.ning.http.client.Response resp) throws Exception {
                return resp.getCookies();
            }
        };
    }

    @Override
    public Observable<Set<WebPageEntity>> parse(WebPageEntity parent) throws Exception {
        List<Cookie> cookies = new LinkedList<>();
        Map<String, String> formParameters = new HashMap<>();
        formParameters.put("vb_login_username", AppProperties.getProperty("canadiangunnutzLogin"));
        formParameters.put("vb_login_password", AppProperties.getProperty("canadiangunnutzPassword"));
        formParameters.put("vb_login_password_hint", "Password");
        formParameters.put("s", "");
        formParameters.put("securitytoken", "guest");
        formParameters.put("do", "login");
        formParameters.put("vb_login_md5password", "");
        formParameters.put("vb_login_md5password_utf", "");

        Future<List<Cookie>> futureCookies = client.post("http://www.canadiangunnutz.com/forum/login.php?do=login", formParameters, cookies, getEngCookiesHandler());

        return Observable.from(futureCookies).map(cookies::addAll).map(success -> {
            return client.get("http://www.canadiangunnutz.com/forum/forum.php", cookies, new AsyncCompletionHandler<Set<WebPageEntity>>() {
                @Override
                public Set<WebPageEntity> onCompleted(Response resp) throws Exception {
                    HashSet<WebPageEntity> result = new HashSet<>();
                    if (200 == resp.getStatusCode()) {
                        Document document = Jsoup.parse(resp.getResponseBody(), parent.getUrl());
                        Elements elements = document.select("h2.forumtitle > a");
                        if (elements.isEmpty()) {
                            logger.error("No results on page");
                        }

                        for (Element element : elements) {
                            String text = element.text();
                            if (!text.startsWith("Exchange of")) {
                                continue;
                            }
                            for (String category : categories) {
                                if (text.endsWith(category)) {
                                    WebPageEntity webPageEntity = new WebPageEntity();
                                    webPageEntity.setUrl(element.attr("abs:href"));
                                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                    webPageEntity.setParsed(false);
                                    webPageEntity.setStatusCode(resp.getStatusCode());
                                    webPageEntity.setType("productList");
                                    webPageEntity.setParent(parent);
                                    logger.info("productList={}, parent={}", webPageEntity.getUrl(), parent.getUrl());
                                    result.add(webPageEntity);
                                    break;
                                }
                            }
                        }

                    } else {
                        logger.error("Failed to load page {}", resp.getUri().toString());
                    }
                    return result;
                }
            });
        }).flatMap(Observable::from).flatMap(Observable::from).map(forumPage -> client.get(forumPage.getUrl(), cookies, new AsyncCompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
                if (200 == resp.getStatusCode()) {
                    Document document = Jsoup.parse(resp.getResponseBody(), resp.getUri().toString());
                    Element element = document.select("#threadpagestats").first();
                    String text = element.text();
                    Matcher matcher = Pattern.compile("Threads (\\d+) to (\\d+) of (\\d+)").matcher(text);
                    if (matcher.find()) {
                        int postsPerPage = Integer.parseInt(matcher.group(2));
                        int total = Integer.parseInt(matcher.group(3));
                        int pages = (int) Math.ceil((double) total / postsPerPage);
                        for (int i = 1; i <= pages; i++) {
                            WebPageEntity webPageEntity = new WebPageEntity();
                            webPageEntity.setUrl(resp.getUri() + "/page" + i);
                            webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                            webPageEntity.setParsed(false);
                            webPageEntity.setStatusCode(resp.getStatusCode());
                            webPageEntity.setType("productList");
                            webPageEntity.setParent(parent);
                            logger.info("productList={}, parent={}", webPageEntity.getUrl(), parent.getUrl());
                            result.add(webPageEntity);
                        }
                    }
                }
                return result;
            }
        })).flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.canadiangunnutz.com/") && webPage.getType().equals("frontPage");
    }
}
