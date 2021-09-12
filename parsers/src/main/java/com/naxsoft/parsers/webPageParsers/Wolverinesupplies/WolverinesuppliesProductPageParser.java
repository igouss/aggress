package com.naxsoft.parsers.webPageParsers.Wolverinesupplies;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.HttpClient;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class WolverinesuppliesProductPageParser extends AbstractWebPageParser {
    private final HttpClient client;

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPage) {
//        log.trace("Processing productPage {}", webPage.getUrl());
//        return Observable.from(PageDownloader.download(client, webPage, "productPageRaw"))
//                .filter(Objects::nonNull)
//                .toList().toBlocking().single();
        return null;
    }

    @Override
    public String getParserType() {
        return "productPage";
    }

    @Override
    public String getSite() {
        return "wolverinesupplies.com";
    }
}
