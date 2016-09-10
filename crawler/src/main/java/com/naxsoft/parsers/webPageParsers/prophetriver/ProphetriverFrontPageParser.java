package com.naxsoft.parsers.webPageParsers.prophetriver;

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
class ProphetriverFrontPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProphetriverFrontPageParser.class);
    private final HttpClient client;

    public ProphetriverFrontPageParser(HttpClient client) {
        this.client = client;
    }

    private static WebPageEntity create(WebPageEntity parent, String url, String category) {
        WebPageEntity webPageEntity = new WebPageEntity(parent, "", "productList", false, url, category);
        return webPageEntity;
    }

    private Observable<WebPageEntity> parseFrontPage(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("#AccordianWrapper a");
            for (Element e : elements) {
                result.add(create(downloadResult.getSourcePage(), e.attr("abs:href"), null));
            }
        }
        return Observable.from(result);
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return client.get("http://store.prophetriver.com/categories/", new DocumentCompletionHandler(parent))
                .flatMap(this::parseFrontPage);

    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("prophetriver.com") && webPage.getType().equals("frontPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("prophetriver.com/frontPage", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("webPageParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}