package com.naxsoft.parsers.webPageParsers.dantesports;

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
import java.util.LinkedList;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
class DantesportsFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(DantesportsFrontPageParser.class);
    private final HttpClient client;

    public DantesportsFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private Observable<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#scol1 > div.scell_menu > li > a");

            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(downloadResult.getSourcePage(), "", "productList", false, element.attr("abs:href") + "&paging=0", element.text());
                LOGGER.info("productList={}", webPageEntity.getUrl());
                result.add(webPageEntity);
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return client.get("https://shop.dantesports.com/set_lang.php?lang=EN", new LinkedList<>(), getCookiesHandler(), false)
                .first()
                .flatMap(cookies1 -> client.get(webPage.getUrl(), cookies1, new DocumentCompletionHandler(webPage)))
                .flatMap(this::parseDocument);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("dantesports.com") && webPage.getType().equals("frontPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("dantesports.com/frontPage", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("webPageParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
