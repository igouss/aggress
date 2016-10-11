package com.naxsoft.parsers.webPageParsers.sail;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import org.asynchttpclient.cookie.Cookie;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.*;

/**
 * Copyright NAXSoft 2015
 */
class SailsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SailsFrontPageParser.class);
    private static final Collection<Cookie> cookies;

    static {
        cookies = new ArrayList<>(1);
        cookies.add(Cookie.newValidCookie("store_language", "english", false, null, null, Long.MAX_VALUE, false, false));
    }

    private final HttpClient client;

    private SailsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("ol.nav-2 a");

            for (Element el : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", el.attr("abs:href") + "?limit=36", downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://www.sail.ca/en/hunting/firearms", "firearm"));
        webPageEntities.add(create(parent, "http://www.sail.ca/en/hunting/firearm-accessories", "firearm"));
        webPageEntities.add(create(parent, "http://www.sail.ca/en/hunting/optics-and-shooting-accessories", "optic"));
        webPageEntities.add(create(parent, "http://www.sail.ca/en/hunting/ammunition", "ammo"));
        return Observable.from(webPageEntities)
                .flatMap(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(this::parseDocument);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("sail.ca") && webPage.getType().equals("frontPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("sail.ca/frontPage", (Message<WebPageEntity> event) -> {
            vertx.executeBlocking(future -> {
                Iterator<WebPageEntity> iterator = parse(event.body()).toBlocking().getIterator();
                future.complete(iterator);
            }, (AsyncResult<Iterator<WebPageEntity>> result) -> {
                if (result.succeeded()) {
                    Iterator<WebPageEntity> it = result.result();
                    while (it.hasNext()) {
                        vertx.eventBus().publish("webPageParseResult", it.next());
                    }
                } else {
                    LOGGER.error("Failed to parse", result.cause());
                }
            });
        });
    }
}