package com.naxsoft.parsers.webPageParsers.marstar;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import rx.Observable;

import java.util.HashSet;

/**
 * Copyright NAXSoft 2015
 */
public class MarstarFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;

    public MarstarFrontPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://www.marstar.ca/dynamic/category.jsp?catid=1")); // firearms
        webPageEntities.add(create("http://www.marstar.ca/dynamic/category.jsp?catid=3")); // ammo
        webPageEntities.add(create("http://www.marstar.ca/dynamic/category.jsp?catid=81526")); // Firearms

        return Observable.from(webPageEntities);
    }

    private static WebPageEntity create(String url) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setParsed(false);
        webPageEntity.setType("productList");
        webPageEntity.setCategory("n/a");
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().equals("http://www.marstar.ca/") && webPage.getType().equals("frontPage");
    }
}

