package com.naxsoft.crawler.parsers.parsers.webPageParsers.canadiangunnutz;

import com.naxsoft.common.entity.WebPageEntity;
import com.naxsoft.crawler.parsers.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.http.HttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
class CanadiangunnutzProductPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private final List<Cookie> cookies = new ArrayList<>();

//    private CanadiangunnutzProductPageParser(HttpClient client) {
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

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPage) {
        log.trace("Processing productPage {}", webPage.getUrl());

        return null;
//        return Observable.from(PageDownloader.download(client, cookies, webPage, "productPageRaw"))
//                .toList().toBlocking().single();
    }

    @Override
    public String getParserType() {
        return "productPage";
    }

    @Override
    public String getSite() {
        return "canadiangunnutz.com";
    }
}
