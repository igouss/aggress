package com.naxsoft.parsers.webPageParsers.marstar;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashSet;

/**
 * Copyright NAXSoft 2015
 */
class MarstarFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MarstarFrontPageParser.class);
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

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("marstar.ca/frontPage", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("webPageParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}

