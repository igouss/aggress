package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class WebPageParserFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebPageParserFactory.class);

    private final Set<WebPageParser> parsers = new HashSet<>();
    private final HttpClient client;

    /**
     * Create new WebPageParserFactory that using reflection to locate all WebPageParsers in the classpath
     *
     * @param client HTTP client for WebPageParsers to use
     */
    @Inject
    public WebPageParserFactory(HttpClient client) {
        this.client = client;

        Reflections reflections = new Reflections("com.naxsoft.parsers.webPageParsers");
        Set<Class<? extends WebPageParser>> classes = reflections.getSubTypesOf(WebPageParser.class);

        Class<?> asyncFetchClient = null;
        for (Class iface : client.getClass().getInterfaces()) {
            if (iface.getCanonicalName().equals("com.naxsoft.crawler.HttpClient")) {
                asyncFetchClient = iface;
            }
        }

        for (Class<? extends WebPageParser> clazz : classes) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    LOGGER.info("Instantiating {}", clazz.getName());

                    Constructor<? extends WebPageParser> constructor = clazz.getDeclaredConstructor(asyncFetchClient);
                    constructor.setAccessible(true);

                    WebPageParser webPageParser = constructor.newInstance(client);
                    this.parsers.add(webPageParser);
                } catch (Exception e) {
                    LOGGER.error("Failed to instantiate WebPage parser {}", clazz, e);
                }
            }
        }
    }

    /**
     * Get a WebPageParser that is capable of parsing webPageEntity
     *
     * @param webPageEntity Page to parse
     * @return WebPageParser that if capable of parsing webPageEntity
     */
    public WebPageParser getParser(WebPageEntity webPageEntity) {
        for (WebPageParser parser : parsers) {
            if (parser.canParse(webPageEntity)) {
                LOGGER.debug("Found a parser {} for action = {} url = {}", parser.getClass(), webPageEntity.getType(), webPageEntity.getUrl());
                return parser;
            }
        }
        LOGGER.warn("Failed to find a web-page parser for action = {}, url = {}", webPageEntity.getType(), webPageEntity.getUrl());
        return new NoopParser(client);
    }
}
