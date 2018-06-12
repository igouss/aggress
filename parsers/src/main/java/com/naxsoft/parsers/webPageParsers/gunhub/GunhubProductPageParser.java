package com.naxsoft.parsers.webPageParsers.gunhub;

import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.http.HttpClient;
import com.naxsoft.http.PageDownloader;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.List;
import java.util.Objects;

public class GunhubProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(GunhubProductPageParser.class);

    public GunhubProductPageParser(HttpClient client) {
        super(client);
    }

    @Override
    public List<WebPageEntity> parse(WebPageEntity webPage) {
        LOGGER.trace("Processing productPage {}", webPage.getUrl());
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
        return "gunhub.ca";
    }
}