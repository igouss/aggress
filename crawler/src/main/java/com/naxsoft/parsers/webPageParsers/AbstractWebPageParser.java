package com.naxsoft.parsers.webPageParsers;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.AbstractCompletionHandler;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import org.asynchttpclient.Response;
import org.asynchttpclient.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Disposable;

import java.util.List;


public abstract class AbstractWebPageParser extends AbstractVerticle implements WebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebPageParser.class);
    protected final Counter parseResultCounter;
    protected final HttpClient client;
    private final Handler<Message<WebPageEntity>> messageHandler;
    private Disposable webPageParseResult;


    public AbstractWebPageParser(MetricRegistry metricRegistry, HttpClient client) {
        this.client = client;

        String metricName = MetricRegistry.name(getSite().replaceAll("\\.", "_") + "." + getParserType(), "parseResults");
        parseResultCounter = metricRegistry.counter(metricName);

        messageHandler = event -> webPageParseResult = parse(event.body()).subscribe(value -> {
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

    /**
     * @return website this parser can parse
     */
    protected abstract String getSite();

    /**
     * @return type of the page this parser can parse
     */
    protected abstract String getParserType();

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer(getSite() + "/" + getParserType(), messageHandler);
    }

    @Override
    public void stop() throws Exception {
        webPageParseResult.dispose();
    }
}
