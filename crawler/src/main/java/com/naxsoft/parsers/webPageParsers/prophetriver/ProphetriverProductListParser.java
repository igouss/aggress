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
import rx.schedulers.Schedulers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
class ProphetriverProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProphetriverProductListParser.class);
    private final HttpClient client;

    public ProphetriverProductListParser(HttpClient client) {
        this.client = client;
    }

    private Collection<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select(".ProductLink a");
            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productPage", false, element.attr("abs:href"), null);
                LOGGER.info("productPageUrl={}, parent={}", webPageEntity.getUrl(), downloadResult.getSourcePage().getUrl());
                result.add(webPageEntity);
            }
        }
        return result;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        Future<DownloadResult> future = client.get(webPageEntity.getUrl(), new DocumentCompletionHandler(webPageEntity));
        return Observable.from(future, Schedulers.io()).map(this::parseDocument).flatMap(Observable::from);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("prophetriver.com") && webPage.getType().equals("productList");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("prophetriver.com/productList", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("webPageParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}