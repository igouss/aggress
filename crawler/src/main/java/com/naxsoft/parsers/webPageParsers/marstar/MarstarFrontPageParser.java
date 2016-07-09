package com.naxsoft.parsers.webPageParsers.marstar;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import rx.Observable;

import java.util.HashSet;

/**
 * Copyright NAXSoft 2015
 */
class MarstarFrontPageParser extends AbstractWebPageParser {
    private final HttpClient client;

    public MarstarFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(String url, String category) {
        WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productList", false, url, category);
        return webPageEntity;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://www.marstar.ca/dynamic/category.jsp?catid=1", "firearm")); // firearms
        webPageEntities.add(create("http://www.marstar.ca/dynamic/category.jsp?catid=3", "ammo")); // ammo
        webPageEntities.add(create("http://www.marstar.ca/dynamic/category.jsp?catid=81526", "firearm")); // Firearms

        return Observable.from(webPageEntities);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("marstar.ca") && webPage.getType().equals("frontPage");
    }
}

