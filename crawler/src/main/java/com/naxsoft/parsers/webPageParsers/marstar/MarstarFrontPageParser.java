package com.naxsoft.parsers.webPageParsers.marstar;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import rx.Observable;

import java.sql.Timestamp;
import java.util.HashSet;

/**
 * Copyright NAXSoft 2015
 */
public class MarstarFrontPageParser extends AbstractWebPageParser {
    private final AsyncFetchClient client;

    public MarstarFrontPageParser(AsyncFetchClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) throws Exception {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create("http://www.marstar.ca/dynamic/category.jsp?catid=1", parent)); // firearms
        webPageEntities.add(create("http://www.marstar.ca/dynamic/category.jsp?catid=3", parent)); // ammo
        webPageEntities.add(create("http://www.marstar.ca/dynamic/category.jsp?catid=81526", parent)); // Firearms

        return Observable.from(webPageEntities);
    }

    private WebPageEntity create(String url, WebPageEntity parent) {
        WebPageEntity webPageEntity = new WebPageEntity();
        webPageEntity.setUrl(url);
        webPageEntity.setModificationDate(new Timestamp(System.currentTimeMillis()));
        webPageEntity.setParsed(false);
        webPageEntity.setStatusCode(200);
        webPageEntity.setType("productList");
        return webPageEntity;
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().equals("http://www.marstar.ca/") && webPage.getType().equals("frontPage");
    }
}

