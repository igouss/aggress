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
import rx.Observable;
import rx.schedulers.Schedulers;

import java.util.List;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractWebPageParser extends AbstractVerticle implements WebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger("WebPageParser");
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

    protected Handler<Message<WebPageEntity>> getParseRequestMessageHandler() {
        return (Message<WebPageEntity> event) -> {
            Observable<WebPageEntity> webPageEntityObservable = parse(event.body());
            webPageEntityObservable.subscribeOn(Schedulers.io()).subscribe(value -> {
                vertx.eventBus().publish("webPageParseResult", value);
            }, error -> {
                LOGGER.error("Failed to parse", error);
            });
        };
    }

}
