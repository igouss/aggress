package com.naxsoft.parsers.webPageParsers;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.WebPageEntity;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Copyright NAXSoft 2015
 */
class NoopParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoopParser.class);

    public NoopParser(MetricRegistry metricRegistry, HttpClient client) {
        super(metricRegistry, client);
    }

    @Override
    public Iterable<WebPageEntity> parse(WebPageEntity webPage) {
        LOGGER.error("Using NOOP parser for: " + webPage);
        return Set.of();
    }

    @Override
    public String getParserType() {
        return "noopWebPageParser";
    }

    @Override
    public String getSite() {
        return "anySite";
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("noopWebPageParser", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("webPageParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
