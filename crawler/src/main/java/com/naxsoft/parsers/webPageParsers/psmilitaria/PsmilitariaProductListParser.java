package com.naxsoft.parsers.webPageParsers.psmilitaria;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import com.naxsoft.parsers.webPageParsers.prophetriver.ProphetriverProductListParser;
import com.ning.http.client.ListenableFuture;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class PsmilitariaProductListParser extends AbstractWebPageParser {
    private final HttpClient client;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProphetriverProductListParser.class);

    public PsmilitariaProductListParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(parent.getUrl());
        webPageEntity.setParsed(false);
        webPageEntity.setCategory(parent.getCategory());
        webPageEntity.setType("productPage");
        webPageEntity.setContent(parent.getContent());
        return Observable.just(webPageEntity);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://psmilitaria.50megs.com/") && webPage.getType().equals("productList");
    }
}