package com.naxsoft.parsers.webPageParsers.canadiangunnutz;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
import io.netty.handler.codec.http.cookie.Cookie;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
class CanadiangunnutzFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadiangunnutzFrontPageParser.class);

    private static final Map<String, String> categories = new HashMap<>();
    private static final Pattern threadsPattern = Pattern.compile("Threads (\\d+) to (\\d+) of (\\d+)");

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

    private List<Cookie> cookies = null;

    private CanadiangunnutzFrontPageParser() {
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
            cookies = client.post("http://www.canadiangunnutz.com/forum/login.php?do=login", formParameters, new LinkedList<>(), getCookiesHandler()).toBlocking().first();
        } catch (PropertyNotFoundException e) {
            e.printStackTrace();
        }
    }

    private Iterable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
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
                        WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "productList", element.attr("abs:href"), categories.get(category));
                        LOGGER.info("productList={}", webPageEntity.getUrl());
                        result.add(webPageEntity);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private Iterable<WebPageEntity> parseDocument2(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Element element = document.select("#threadpagestats").first();
            String text = element.text();
            Matcher matcher = threadsPattern.matcher(text);
            if (matcher.find()) {
                int postsPerPage = Integer.parseInt(matcher.group(2));
                int total = Integer.parseInt(matcher.group(3));
                int pages = (int) Math.ceil((double) total / postsPerPage);
                for (int i = 1; i <= pages; i++) {
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "productList", document.location() + "/page" + i,
                            downloadResult.getSourcePage().getCategory());
                    LOGGER.info("productList={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            }
        }
        return result;
    }

    @Override
    public Iterable<WebPageEntity> parse(WebPageEntity parent) {
        WebPageEntity webPageEntity = new WebPageEntity(parent, "", "http://www.canadiangunnutz.com/forum/forum.php", "");
        return client.get(webPageEntity.getUrl(), cookies, new DocumentCompletionHandler(webPageEntity))
                .flatMap(this::parseDocument)
                .flatMap(webPageEntity1 -> client.get(webPageEntity1.getUrl(), cookies, new DocumentCompletionHandler(webPageEntity1)))
                .flatMap(this::parseDocument2)
                .doOnNext(e -> this.parseResultCounter.inc());
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "canadiangunnutz.com";
    }

}
