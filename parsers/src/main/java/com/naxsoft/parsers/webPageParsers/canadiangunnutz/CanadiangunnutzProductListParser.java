package com.naxsoft.parsers.webPageParsers.canadiangunnutz;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.DownloadResult;
import com.naxsoft.http.HttpClient;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
class CanadiangunnutzProductListParser extends AbstractWebPageParser {
    private final HttpClient client;
    private final List<Cookie> cookies = new ArrayList<>();

//    private CanadiangunnutzProductListParser(HttpClient client) {
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
//            cookies.addAll(client.post("http://www.canadiangunnutz.com/forum/login.php?do=login", formParameters, new LinkedList<>(), getCookiesHandler()).get());
//        } catch (PropertyNotFoundException | InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
//    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#threads .threadtitle");
            if (elements.isEmpty()) {
                log.error("No results on page");
            }
            for (Element element : elements) {
                Elements select = element.select(".prefix");
                if (!select.isEmpty()) {
                    if (select.first().text().contains("WTS")) {
                        element = element.select("a.title").first();
                        if (!element.text().toLowerCase().contains("remove")) {
                            WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", element.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                            log.info("productPage={}", webPageEntity.getUrl());
                            result.add(webPageEntity);
                        }
                    }
                }
            }
        }
        return result;
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPageEntity) {
        return null;

//        return Observable.from(client.get(webPageEntity.getUrl(), cookies, new DocumentCompletionHandler(webPageEntity)))
//                .map(this::parseDocument)
//                .flatMap(Observable::from)
//                .toList().toBlocking().single();
    }

    @Override
    public String getParserType() {
        return "productList";
    }

    @Override
    public String getSite() {
        return "canadiangunnutz.com";
    }
}
