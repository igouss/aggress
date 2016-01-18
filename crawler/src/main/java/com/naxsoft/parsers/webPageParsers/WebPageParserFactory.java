//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InvalidClassException;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
public class WebPageParserFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebPageParserFactory.class);

    private final Set<WebPageParser> parsers = new HashSet<>();
    private final HttpClient client;

    /**
     *
     * @param client
     */
    public WebPageParserFactory(HttpClient client) {
        this.client = client;

        Reflections reflections = new Reflections("com.naxsoft.parsers.webPageParsers");
        Set<Class<? extends WebPageParser>> classes = reflections.getSubTypesOf(WebPageParser.class);

        for (Class<? extends WebPageParser> clazz : classes) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    LOGGER.info("Instantiating {}", clazz.getName());
                    Class<?>[] interfaces = client.getClass().getInterfaces();
                    Class<?> asyncFetchClient = null;
                    for (Class iface : interfaces) {
                        if (iface.getCanonicalName().equals("com.naxsoft.crawler.HttpClient")) {
                            asyncFetchClient = iface;
                        }
                    }
                    if (null == asyncFetchClient) {
                        throw new InvalidClassException("Class " + clazz.getCanonicalName() + "should implement com.naxsoft.crawler.HttpClient");
                    }
                    WebPageParser webPageParser = clazz.getConstructor(asyncFetchClient).newInstance(client);
                    this.parsers.add(webPageParser);
                } catch (Exception e) {
                    LOGGER.error("Failed to instantiate WebPage parser {}", clazz, e);
                }
            }
        }
    }

    /**
     *
     * @param webPageEntity
     * @return
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
