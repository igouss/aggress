package com.naxsoft.parsers.webPageParsers.psmilitaria;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * Copyright NAXSoft 2015
 */
public class PsmilitariaProductParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(PsmilitariaProductParser.class);
    private final HttpClient client;

    public PsmilitariaProductParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(parent.getUrl());
        webPageEntity.setParsed(false);
        webPageEntity.setCategory(parent.getCategory());
        webPageEntity.setType("productPageRaw");
        webPageEntity.setContent(parent.getContent());
        return Observable.just(webPageEntity);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://psmilitaria.50megs.com/") && webPage.getType().equals("productPage");
    }
}