package com.naxsoft.crawler.parsers.parsers.webPageParsers.crafm;

import com.naxsoft.common.entity.WebPageEntity;
import com.naxsoft.crawler.parsers.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.http.HttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
class CrafmProductPageParser extends AbstractWebPageParser {
    private final HttpClient client;
    private final List<Cookie> cookies;

//    private CrafmProductPageParser(HttpClient client) {
//        cookies = new ArrayList<>(1);
//        Cookie.Builder builder = new Cookie.Builder();
//        builder.name("store").value("english").domain("crafm.com");
//        cookies.add(builder.build());
//    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPage) {
        log.trace("Processing productPage {}", webPage.getUrl());
        return null;
//        return Observable.from(PageDownloader.download(client, cookies, webPage, "productPageRaw"))
//                .filter(Objects::nonNull)
//                .toList().toBlocking().single();
    }

    @Override
    public String getParserType() {
        return "productPage";
    }

    @Override
    public String getSite() {
        return "crafm.com";
    }


}
