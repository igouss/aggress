package com.naxsoft.parsers.webPageParsers.canadaAmmo;

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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

/**
 * Copyright NAXSoft 2015
 */
class CanadaAmmoProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CanadaAmmoProductListParser.class);
    private final HttpClient client;

    public CanadaAmmoProductListParser(HttpClient client) {
        this.client = client;
    }

    private Set<WebPageEntity> parseDocument(DownloadResult downloadResult) {
        Set<WebPageEntity> result = new HashSet<>(1);

        Document document = downloadResult.getDocument();
        if (document != null) {
            Elements elements = document.select("a.product__link");
            for (Element element : elements) {
                WebPageEntity webPageEntity = new WebPageEntity(0L, "", "productPage", false, element.attr("abs:href"), downloadResult.getSourcePage().getCategory());
                LOGGER.info("productPage={}", webPageEntity.getUrl());
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
        return webPage.getUrl().contains("canadaammo.com") && webPage.getType().equals("productList");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("canadaammo.com/productList", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("webPageParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
