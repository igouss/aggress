package com.naxsoft.parsers.webPageParsers.frontierfirearms;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
class FrontierfirearmsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FrontierfirearmsFrontPageParser.class);
    private final HttpClient client;

    private FrontierfirearmsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        return new WebPageEntity(parent, "", "productList", url, category);
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(2);

        Document document = downloadResult.getDocument();
        if (document != null) {
            if (document.select("#CategoryPagingBottom > div > a").isEmpty()) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location(), downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            } else {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", document.location(), downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);

                // select next active
                Elements select = document.select(".PagingList .ActivePage + li a");
                if (!select.isEmpty()) {
                    webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", select.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                    LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                    result.add(webPageEntity);
                }
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        HashSet<WebPageEntity> webPageEntities = new HashSet<>();
        webPageEntities.add(create(parent, "http://frontierfirearms.ca/firearms.html", "firearm"));
        webPageEntities.add(create(parent, "http://frontierfirearms.ca/ammunition-reloading.html", "ammo"));
        webPageEntities.add(create(parent, "http://frontierfirearms.ca/shooting-accessories.html", "misc"));
        webPageEntities.add(create(parent, "http://frontierfirearms.ca/optics.html", "optic"));
        return Observable.from(webPageEntities)
                .flatMap(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .filter(downloadResult -> {
                    if (downloadResult == null) {
                        LOGGER.error("Failed to get download results");
                        return false;
                    }
                    return true;
                })
                .flatMap(this::parseDocument);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("frontierfirearms.ca") && webPage.getType().equals("frontPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("frontierfirearms.ca/frontPage", (Message<WebPageEntity> event) -> {
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