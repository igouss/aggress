package com.naxsoft.parsers.productParser;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.naxsoft.entity.WebPageEntity;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Copyright NAXSoft 2015
 */
abstract class AbstractRawPageParser extends AbstractVerticle implements ProductParser {
    private static final Logger LOGGER = LoggerFactory.getLogger("RawPageParser");
    private final Counter parseResultCounter;

    private Disposable productParseResult;

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
    String getParserType() {
        return "productPageRaw";
    }

    @Override
    public void start() throws Exception {
        vertx.eventBus().consumer(getSite() + "/" + getParserType(), (Message<WebPageEntity> event) -> productParseResult = Observable.fromIterable(parse(event.body()))
                .subscribeOn(Schedulers.io())
                .doOnNext(productEntity -> parseResultCounter.inc())
                .subscribe(
                        message -> vertx.eventBus().publish("productParseResult", message),
                        err -> LOGGER.error("Failed to parse", err)));
    }

    @Override
    public void stop() throws Exception {
        productParseResult.dispose();
    }
}
