package com.naxsoft.crawler.parsers.parsers.webPageParsers.canadiangunnutz;

import com.naxsoft.common.entity.WebPageEntity;
import com.naxsoft.crawler.parsers.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.http.DownloadResult;
import com.naxsoft.http.HttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
class CanadiangunnutzFrontPageParser extends AbstractWebPageParser {
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

    private final HttpClient client;
    private final List<Cookie> cookies = null;

//    private CanadiangunnutzFrontPageParser(HttpClient client) {
//        Map<String, String> formParameters = new HashMap<>();
//        try {
//            formParameters.put("vb_login_username", AppProperties.getProperty("canadiangunnutzLogin"));
//            formParameters.put("vb_login_password", AppProperties.getProperty("canadiangunnutzPassword"));
//            formParameters.put("vb_login_password_hint", "Password");
//            formParameters.put("s", "");
//            formParameters.put("securitytoken", "guest");
//            formParameters.put("do", "login");
//            formParameters.put("vb_login_md5password", "");
//            formParameters.put("vb_login_md5password_utf", "");
//            cookies = client.post("http://www.canadiangunnutz.com/forum/login.php?do=login", formParameters, new LinkedList<>(), getCookiesHandler()).get();
//        } catch (PropertyNotFoundException | InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
//    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("h2.forumtitle > a");
            if (elements.isEmpty()) {
                log.error("No results on page");
            }

            for (Element element : elements) {
                String text = element.text();
                if (!text.startsWith("Exchange of")) {
                    continue;
                }
                for (String category : categories.keySet()) {
                    if (text.endsWith(category)) {
                        WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", element.attr("abs:href"), categories.get(category));
                        log.info("productList={}", webPageEntity.getUrl());
                        result.add(webPageEntity);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private Set<WebPageEntity> parseDocument2(DownloadResult downloadResult) {
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
                    WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location() + "/page" + i, downloadResult.getSourcePage().getCategory());
                    log.info("productList={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            }
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return null;
//        WebPageEntity page = new WebPageEntity(webPageEntity, "", "", "http://www.canadiangunnutz.com/forum/forum.php", "");
//        return Observable.from(client.get(page.getUrl(), cookies, new DocumentCompletionHandler(page)))
//                .map(this::parseDocument)
//                .flatMap(Observable::from)
//                .map(webPageEntity1 -> client.get(webPageEntity1.getUrl(), cookies, new DocumentCompletionHandler(webPageEntity1)))
//                .flatMap(Observable::from)
//                .map(this::parseDocument2)
//                .flatMap(Observable::from)
//                .toList().toBlocking().single();
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
