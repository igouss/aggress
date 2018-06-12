package com.naxsoft.parsers.webPageParsers.tradeexcanada;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.HttpClient;
import com.naxsoft.http.PageDownloader;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.ArrayList;
import java.util.List;

class TradeexCanadaProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TradeexCanadaProductPageParser.class);

    public TradeexCanadaProductPageParser(HttpClient client) {
        super(client);
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPage) {
        LOGGER.trace("Processing productPage {}", webPage.getUrl());
        if (webPage.getUrl().contains("out-stock") || webPage.getUrl().contains("-sold")) {
            return new ArrayList<>(0);
        } else {
            return Observable.from(PageDownloader.download(client, webPage, "productPageRaw"))
                    .filter(data -> {
                        if (null != data) {
                            return true;
                        } else {
                            LOGGER.error("failed to download web page {}", webPage.getUrl());
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