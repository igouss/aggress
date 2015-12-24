package com.naxsoft.parsers.webPageParsers.canadiangunnutz;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import com.naxsoft.utils.AppProperties;
import com.ning.http.client.AsyncCompletionHandler;
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

import java.sql.Timestamp;
import java.util.*;

/**
 * Copyright NAXSoft 2015
 */
public class CanadiangunnutzProductListParser implements WebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(CanadiangunnutzProductListParser.class);
    private final AsyncFetchClient client;
    private List<Cookie> cookies;

    public CanadiangunnutzProductListParser(AsyncFetchClient client) {
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

        ListenableFuture<List<Cookie>> futureCookies = client.post("http://www.canadiangunnutz.com/forum/login.php?do=login", formParameters, new LinkedHashSet<>(), getEngCookiesHandler());
        try {
            cookies = futureCookies.get();
        } catch (Exception e) {
            logger.error("Failed to login to canadiangunnutz", e);
        }
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
    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPage) throws Exception {
        if (cookies.isEmpty()) {
            logger.warn("No login cookies");
            return Observable.empty();
        }

        return Observable.from(client.get(webPage.getUrl(), cookies, new AsyncCompletionHandler<Set<WebPageEntity>>() {
            @Override
            public Set<WebPageEntity> onCompleted(Response resp) throws Exception {
                HashSet<WebPageEntity> result = new HashSet<>();
                if (200 == resp.getStatusCode()) {
                    Document document = Jsoup.parse(resp.getResponseBody(), webPage.getUrl());
                    Elements elements = document.select("#threads .threadtitle");
                    if (elements.isEmpty()) {
                        logger.error("No results on page");
                    }
                    for (Element element : elements) {
                        Elements select = element.select(".prefix");
                        if (!select.isEmpty()) {
                            if (select.first().text().contains("WTS")) {
                                element = element.select("a.title").first();
                                if (!element.text().toLowerCase().contains("remove")) {
                                    WebPageEntity webPageEntity = new WebPageEntity();
                                    webPageEntity.setUrl(element.attr("abs:href"));
                                    webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
                                    webPageEntity.setParsed(false);
                                    webPageEntity.setType("productPage");
                                    webPageEntity.setParent(webPage);
                                    webPageEntity.setStatusCode(resp.getStatusCode());
                                    logger.info("productPage={}", webPageEntity.getUrl());
                                    result.add(webPageEntity);
                                }
                            }
                        }
                    }
                } else {
                    logger.error("Failed to load page {}", webPage.getUrl());
                }
                return result;
            }
        }));

    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.canadiangunnutz.com/") && webPage.getType().equals("productList");
    }
}
