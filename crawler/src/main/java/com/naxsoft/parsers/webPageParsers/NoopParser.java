package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

/**
 * Copyright NAXSoft 2015
 */
class NoopParser extends AbstractWebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoopParser.class);

    private NoopParser(HttpClient client) {
    }

    @Override
    public Observable<WebPageEntity> parse(WebPageEntity webPage) {
        LOGGER.error("Using NOOP parser for: " + webPage);

        return Observable.empty();
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
