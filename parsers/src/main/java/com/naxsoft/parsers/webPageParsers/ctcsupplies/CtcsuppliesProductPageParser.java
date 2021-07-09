package com.naxsoft.parsers.webPageParsers.ctcsupplies;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.HttpClient;
import com.naxsoft.http.PageDownloader;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.List;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
class CtcsuppliesProductPageParser extends AbstractWebPageParser {
    private final HttpClient client;

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPage) {
        log.trace("Processing productPage {}", webPage.getUrl());
        return Observable.from(PageDownloader.download(client, webPage, "productPageRaw"))
                .filter(Objects::nonNull)
                .toList().toBlocking().single();
    }

    @Override
    public String getParserType() {
        return "productPage";
    }

    @Override
    public String getSite() {
        return "ctcsupplies.ca";
    }
}
