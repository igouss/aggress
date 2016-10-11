package com.naxsoft.parsers.webPageParsers.prophetriver;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.Iterator;

/**
 * Copyright NAXSoft 2015
 */
class ProphetriverProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProphetriverProductPageParser.class);
    private final HttpClient client;

    private ProphetriverProductPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        return PageDownloader.download(client, webPage, "productPageRaw").filter(data -> null != data);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("prophetriver.com") && webPage.getType().equals("productPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("prophetriver.com/productPage", (Message<WebPageEntity> event) -> {
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