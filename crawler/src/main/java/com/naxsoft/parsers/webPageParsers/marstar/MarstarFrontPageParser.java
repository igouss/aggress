package com.naxsoft.parsers.webPageParsers.marstar;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashSet;

/**
 * Copyright NAXSoft 2015
 */
class MarstarFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarstarFrontPageParser.class);

    private MarstarFrontPageParser(HttpClient client) {

    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://www.marstar.ca/dynamic/category.jsp?catid=1", "firearm")); // firearms
        webPageEntities.add(create(parent, "http://www.marstar.ca/dynamic/category.jsp?catid=3", "ammo")); // ammo
        webPageEntities.add(create(parent, "http://www.marstar.ca/dynamic/category.jsp?catid=81526", "firearm")); // Firearms

        return Observable.from(webPageEntities);
    }

    @Override
    public String getParserType() {
        return "frontPage";
    }

    @Override
    public String getSite() {
        return "marstar.ca";
    }

}

