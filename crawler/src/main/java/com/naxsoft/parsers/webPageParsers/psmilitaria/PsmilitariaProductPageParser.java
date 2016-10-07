package com.naxsoft.parsers.webPageParsers.psmilitaria;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.parsers.webPageParsers.AbstractWebPageParser;
import com.naxsoft.parsers.webPageParsers.PageDownloader;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * Copyright NAXSoft 2015
 */
class PsmilitariaProductPageParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(PsmilitariaProductPageParser.class);
    private final HttpClient client;

    private PsmilitariaProductPageParser(HttpClient client) {
        this.client = client;
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity parent) {
        return PageDownloader.download(client, parent, "productPageRaw")
                .filter(data -> null != data);

    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("psmilitaria.50megs.com") && webPage.getType().equals("productPage");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus()
                .consumer("psmilitaria.50megs.com/productPage", (Message<WebPageEntity> event) -> {
                    LOGGER.info("Received a new parse request");
                    parse(event.body()).subscribe(
                            webPageEntity -> {
                                LOGGER.info(webPageEntity.toString());
                                vertx.eventBus().publish("webPageParseResult", webPageEntity);
                            },
                            err -> LOGGER.error("Failed to parse", err));
                });
    }
}