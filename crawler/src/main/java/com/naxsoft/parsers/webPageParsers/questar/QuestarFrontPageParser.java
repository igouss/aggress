package com.naxsoft.parsers.webPageParsers.questar;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
class QuestarFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestarFrontPageParser.class);
    private final HttpClient client;

    private QuestarFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#catnav > li > a");

            for (Element e : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "tmp", e.attr("abs:href"), e.text());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return Observable.from(result);
    }

    private Observable<WebPageEntity> parseSubPages(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>();

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements;
            // add subcatogories
            elements = document.select("#main > table > tbody > tr > td > p:nth-child(1) > strong > a");
            for (Element e : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", e.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
            // add product page
            elements = document.select("form > table > tbody > tr > td a");
            for (Element e : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", e.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                LOGGER.info("Product page listing={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return client.get(parent.getUrl(), new DocumentCompletionHandler(parent))
                .flatMap(this::parseDocument)
                .flatMap(webPageEntity -> client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity)))
                .flatMap(this::parseSubPages);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("shopquestar.com") && webPage.getType().equals("frontPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("shopquestar.com/frontPage", (Message<WebPageEntity> event) -> {
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

