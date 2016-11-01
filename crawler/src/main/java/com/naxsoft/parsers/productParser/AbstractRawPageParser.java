package com.naxsoft.parsers.productParser;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
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
    protected final Counter parseResultCounter;

    private Subscription productParseResult;

    public AbstractRawPageParser(MetricRegistry metricRegistry) {
        String metricName = MetricRegistry.name(getSite().replaceAll("\\.", "_") + "." + getParserType(), "parseResults");
        parseResultCounter = metricRegistry.counter(metricName);
    }

    /**
     * @return website this parser can parse
     */
    abstract String getSite();

    /**
     * @return type of the page this parser can parse
     */
    abstract String getParserType();

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(getSite() + "/" + getParserType(), (Message<WebPageEntity> event) -> {
            productParseResult = parse(event.body()).subscribe(message -> vertx.eventBus().publish("productParseResult", message), err -> LOGGER.error("Failed to parse", err));
        });
    }

    @Override
    public void stop() throws Exception {
        productParseResult.unsubscribe();
    }
}
