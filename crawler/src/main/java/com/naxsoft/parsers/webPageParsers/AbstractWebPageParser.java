package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.entity.WebPageEntity;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import org.asynchttpclient.Response;
import org.asynchttpclient.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscription;

import java.util.List;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractWebPageParser extends AbstractVerticle implements WebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebPageParser.class);
    protected Handler<Message<WebPageEntity>> messageHandler;
    private Subscription webPageParseResult;

    public AbstractWebPageParser() {
        messageHandler = event -> webPageParseResult = AbstractWebPageParser.this.parse(event.body()).subscribe(value -> {
            vertx.eventBus().publish("webPageParseResult", value);
        }, error -> {
            LOGGER.error("Failed to parse {}", event.body().getUrl(), error);
        });
    }

    /**
     * @return HTTP cookie handler
     */
    protected static AbstractCompletionHandler<List<Cookie>> getCookiesHandler() {
        /*Return all the cookies contained in HTTP server response*/
        return new AbstractCompletionHandler<List<Cookie>>() {
            private final Logger LOGGER = LoggerFactory.getLogger("com.naxsoft.parsers.webPageParsers.CookieCompletionHandler");

            @Override
            public List<Cookie> onCompleted(Response response) throws Exception {
                LOGGER.info("Completed request to {}", response.getUri().toString());
                return response.getCookies();
            }
        };
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer(getSite() + "/" + getParserType(), messageHandler);
    }

    @Override
    public void stop() throws Exception {
        webPageParseResult.unsubscribe();
    }
}
