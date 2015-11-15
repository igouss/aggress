//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashSet;
import java.util.Set;

public class WebPageParserFactory {
    private static final Logger logger = LoggerFactory.getLogger(WebPageParserFactory.class);

    private Set<WebPageParser> parsers = new HashSet<>();
    private AsyncFetchClient client;
    private MetricRegistry metricRegistry;

    public WebPageParserFactory(AsyncFetchClient client, MetricRegistry metricRegistry) {
        this.client = client;
        this.metricRegistry = metricRegistry;

        Reflections reflections = new Reflections("com.naxsoft.parsers.webPageParsers");
        Set<Class<? extends WebPageParser>> classes = reflections.getSubTypesOf(WebPageParser.class);

        for (Class<? extends WebPageParser> clazz : classes) {
            try {
                WebPageParser webPageParser = clazz.getConstructor(client.getClass()).newInstance(client);
                this.parsers.add(webPageParser);
            } catch (Exception e) {
                logger.error("Failed to instantiate WebPage parser {}", clazz, e);
            }
        }
    }

    private WebPageParser getParser(WebPageEntity webPageEntity) {
        for (WebPageParser parser : parsers) {
            if (parser.canParse(webPageEntity)) {
                logger.debug("Found a parser {} for action = {} url = {}", parser.getClass().toString(), webPageEntity.getType(), webPageEntity.getUrl());
                return parser;
            }
        }
        logger.warn("Failed to find a web-page parser for action = {}, url = {}", webPageEntity.getType(), webPageEntity.getUrl());
        return new NoopParser(client);
    }

    public Observable<Set<WebPageEntity>> parse(WebPageEntity webPageEntity) throws Exception {
        WebPageParser parser = getParser(webPageEntity);
        Timer parseTime = metricRegistry.timer(MetricRegistry.name(parser.getClass(), "parseTime"));
        Timer.Context time = parseTime.time();
        Observable<Set<WebPageEntity>> result = parser.parse(webPageEntity);
        time.stop();
        return result;
    }
}
