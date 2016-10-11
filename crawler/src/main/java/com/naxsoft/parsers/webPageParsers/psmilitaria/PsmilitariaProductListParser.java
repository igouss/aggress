package com.naxsoft.parsers.webPageParsers.psmilitaria;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Iterator;

/**
 * Copyright NAXSoft 2015
 */
class PsmilitariaProductListParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(PsmilitariaProductListParser.class);

    private PsmilitariaProductListParser(HttpClient client) {

    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        WebPageEntity webPageEntity = new WebPageEntity(parent, parent.getContent(), "productPage", parent.getUrl(), parent.getCategory());
        return Observable.just(webPageEntity);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("psmilitaria.50megs.com") && webPage.getType().equals("productList");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("psmilitaria.50megs.com/productList", (Message<WebPageEntity> event) -> {
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