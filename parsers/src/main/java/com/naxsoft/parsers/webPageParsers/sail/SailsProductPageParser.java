package com.naxsoft.parsers.webPageParsers.sail;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.HttpClient;
import com.naxsoft.http.PageDownloader;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Cookie;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
class SailsProductPageParser extends AbstractWebPageParser {
    private static final List<Cookie> cookies;

    static {
        cookies = new ArrayList<>(1);
        Cookie.Builder builder = new Cookie.Builder();
        builder.name("store").value("english").domain("sail.ca");
        cookies.add(builder.build());
    }

    private final HttpClient client;

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPage) {
        log.trace("Processing productPage {}", webPage.getUrl());
        return Observable.from(PageDownloader.download(client, cookies, webPage, "productPageRaw"))
                .filter(Objects::nonNull)
                .toList().toBlocking().single();
    }

    @Override
    public String getParserType() {
        return "productPage";
    }

    @Override
    public String getSite() {
        return "sail.ca";
    }
}