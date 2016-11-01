package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.WebPageEntity;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscription;

/**
 * Copyright NAXSoft 2015
 */
abstract class AbstractRawPageParser extends AbstractVerticle implements ProductParser {
    private static final Logger LOGGER = LoggerFactory.getLogger("RawPageParser");

    private Subscription productParseResult;

    abstract String getSite();

    abstract String getType();

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(getSite() + "/" + getType(), (Message<WebPageEntity> event) -> {
            productParseResult = parse(event.body()).subscribe(message -> vertx.eventBus().publish("productParseResult", message), err -> LOGGER.error("Failed to parse", err));
        });
    }

    @Override
    public void stop() throws Exception {
        productParseResult.unsubscribe();
    }
}
