package com.naxsoft.parsers.webPageParsers.leverarms;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.DocumentCompletionHandler;
import com.naxsoft.parsers.webPageParsers.DownloadResult;
import io.vertx.core.eventbus.Message;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
class LeverarmsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeverarmsFrontPageParser.class);
    private final HttpClient client;

    public LeverarmsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#nav-sidebox li:not(.parent) a");

            for (Element e : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", false, e.attr("abs:href") + "?limit=all", e.text());
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
            Elements elements = document.select(".item h4 a");

            for (Element e : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productPage", false, e.attr("abs:href"), downloadResult.getSourcePage().getCategory());
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
                .flatMap(webPageEntity1 -> client.get(webPageEntity1.getUrl(), new DocumentCompletionHandler(webPageEntity1)))
                .flatMap(this::parseSubPages);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("leverarms.com") && webPage.getType().equals("frontPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("leverarms.com/frontPage", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("webPageParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}