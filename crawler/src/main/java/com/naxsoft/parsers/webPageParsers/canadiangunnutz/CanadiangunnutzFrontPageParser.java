package com.naxsoft.parsers.webPageParsers.canadiangunnutz;

import com.naxsoft.crawler.CompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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


    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        try {
            if (null == cookies || cookies.isEmpty()) {
                LOGGER.info("Loading login cookies");
                cookies = futureCookies.get();
            }
            Observable<WebPageEntity> productList = Observable.create(subscriber -> client.get("http://www.canadiangunnutz.com/forum/forum.php", cookies, new VoidCompletionHandler(parent, subscriber)));
            return Observable.create(subscriber -> productList.subscribe(forumPage -> {
                client.get(forumPage.getUrl(), cookies, new VoidCompletionHandler2(parent, subscriber));
            }));
        } catch (Exception e) {
            LOGGER.error("An error occurred", e);
        }
        return null;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.canadiangunnutz.com/") && webPage.getType().equals("frontPage");
    }

    private static class VoidCompletionHandler extends CompletionHandler<Void> {
        private final WebPageEntity parent;
        private final Subscriber<? super WebPageEntity> subscriber;

        public VoidCompletionHandler(WebPageEntity parent, Subscriber<? super WebPageEntity> subscriber) {
            this.parent = parent;
            this.subscriber = subscriber;
        }

        @Override
        public Void onCompleted(Response response) throws Exception {
            if (200 == response.getStatusCode()) {
                Document document = Jsoup.parse(response.getResponseBody(), parent.getUrl());
                parseDocument(document);
            } else {
                LOGGER.error("Failed to load page {}", response.getUri());
            }

            subscriber.onCompleted();
            return null;
        }

        private void parseDocument(Document document) {
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
                        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                        webPageEntity.setParsed(false);
                        webPageEntity.setType("productList");
                        LOGGER.info("productList={}, parent={}", webPageEntity.getUrl(), parent.getUrl());
                        subscriber.onNext(webPageEntity);
                        break;
                    }
                }
            }
        }
    }

    private static class VoidCompletionHandler2 extends CompletionHandler<Void> {
        private final WebPageEntity parent;
        private final Subscriber<? super WebPageEntity> subscriber;

        public VoidCompletionHandler2(WebPageEntity parent, Subscriber<? super WebPageEntity> subscriber) {
            this.parent = parent;
            this.subscriber = subscriber;
        }

        @Override
        public Void onCompleted(Response response) throws Exception {
            if (200 == response.getStatusCode()) {
                Document document = Jsoup.parse(response.getResponseBody(), response.getUri().toString());
                parseDocument(response, document);
            }
            subscriber.onCompleted();
            return null;
        }

        private void parseDocument(Response response, Document document) {
            Element element = document.select("#threadpagestats").first();
            String text = element.text();
            Matcher matcher = Pattern.compile("Threads (\\d+) to (\\d+) of (\\d+)").matcher(text);
            if (matcher.find()) {
                int postsPerPage = Integer.parseInt(matcher.group(2));
                int total = Integer.parseInt(matcher.group(3));
                int pages = (int) Math.ceil((double) total / postsPerPage);
                for (int i = 1; i <= pages; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity();
                    webPageEntity.setUrl(response.getUri() + "/page" + i);
                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                    webPageEntity.setParsed(false);
                    webPageEntity.setType("productList");
                    LOGGER.info("productList={}, parent={}", webPageEntity.getUrl(), parent.getUrl());
                    subscriber.onNext(webPageEntity);
                }
            }
        }
    }
}
