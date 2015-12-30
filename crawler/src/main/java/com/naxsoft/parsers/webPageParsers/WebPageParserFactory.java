//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class WebPageParserFactory {
    private static final Logger logger = LoggerFactory.getLogger(WebPageParserFactory.class);

    private final Set<WebPageParser> parsers = new HashSet<>();
    private final AsyncFetchClient client;

    public WebPageParserFactory(AsyncFetchClient client) {
        this.client = client;

        Reflections reflections = new Reflections("com.naxsoft.parsers.webPageParsers");
        Set<Class<? extends WebPageParser>> classes = reflections.getSubTypesOf(WebPageParser.class);

        for (Class<? extends WebPageParser> clazz : classes) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    logger.info("Instantiating {}", clazz.getName());
                    WebPageParser webPageParser = clazz.getConstructor(client.getClass()).newInstance(client);
                    this.parsers.add(webPageParser);
                } catch (Exception e) {
                    logger.error("Failed to instantiate WebPage parser {}", clazz, e);
                }
            }
        }
    }

    public WebPageParser getParser(WebPageEntity webPageEntity) {
        for (WebPageParser parser : parsers) {
            if (parser.canParse(webPageEntity)) {
                logger.debug("Found a parser {} for action = {} url = {}", parser.getClass(), webPageEntity.getType(), webPageEntity.getUrl());
                return parser;
            }
        }
        logger.warn("Failed to find a web-page parser for action = {}, url = {}", webPageEntity.getType(), webPageEntity.getUrl());
        return new NoopParser(client);
    }
}
