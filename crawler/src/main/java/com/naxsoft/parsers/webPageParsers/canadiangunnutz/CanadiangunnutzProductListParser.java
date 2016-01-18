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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Copyright NAXSoft 2015
 */
public class CanadiangunnutzProductListParser extends AbstractWebPageParser {
    private static final Logger logger = LoggerFactory.getLogger(CanadiangunnutzProductListParser.class);
    private final HttpClient client;
    private final ListenableFuture<List<Cookie>> futureCookies;


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
        return Observable.create(subscriber -> {
            try {
                logger.info("Loading login cookies");
                List<Cookie> cookies = futureCookies.get();
                if (null == cookies || cookies.isEmpty()) {
                    logger.warn("No login cookies");
                    subscriber.onCompleted();
                    return;
                }
                client.get(parent.getUrl(), cookies, new CompletionHandler<Void>() {
                    @Override
                    public Void onCompleted(Response resp) throws Exception {
                        if (200 == resp.getStatusCode()) {
                            Document document = Jsoup.parse(resp.getResponseBody(), parent.getUrl());
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
                                            webPageEntity.setCategory(parent.getCategory());
                                            logger.info("productPage={}", webPageEntity.getUrl());
                                            subscriber.onNext(webPageEntity);
                                        }
                                    }
                                }
                            }
                        } else {
                            logger.error("Failed to load page {}", parent.getUrl());
                        }
                        subscriber.onCompleted();
                        return null;
                    }
                });
            } catch (Exception e) {
                logger.error("Failed to login to canadiangunnutz", e);
            }

        });
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.canadiangunnutz.com/") && webPage.getType().equals("productList");
    }
}
