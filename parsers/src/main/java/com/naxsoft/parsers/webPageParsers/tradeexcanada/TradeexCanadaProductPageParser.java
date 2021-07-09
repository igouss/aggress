package com.naxsoft.parsers.webPageParsers.tradeexcanada;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.HttpClient;
import com.naxsoft.http.PageDownloader;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
class TradeexCanadaProductPageParser extends AbstractWebPageParser {
    private final HttpClient client;

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPage) {
        log.trace("Processing productPage {}", webPage.getUrl());
        if (webPage.getUrl().contains("out-stock") || webPage.getUrl().contains("-sold")) {
            return new ArrayList<>(0);
        } else {
            return Observable.from(PageDownloader.download(client, webPage, "productPageRaw"))
                    .filter(data -> {
                        if (null != data) {
                            return true;
                        } else {
                            log.error("failed to download web page {}", webPage.getUrl());
                            return false;
                        }
                    }).toList().toBlocking().single();
        }
    }

    @Override
    public String getParserType() {
        return "productPage";
    }

    @Override
    public String getSite() {
        return "tradeexcanada.com";
    }
}