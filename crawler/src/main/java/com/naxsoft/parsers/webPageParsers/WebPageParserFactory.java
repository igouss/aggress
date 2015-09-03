package com.naxsoft.parsers.webPageParsers;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class WebPageParserFactory {
    private Set<WebPageParser> parsers = new HashSet<>();
    private final Logger logger;

    public WebPageParserFactory() {
        logger = LoggerFactory.getLogger(WebPageParserFactory.class);
        Reflections reflections = new Reflections("com.naxsoft.parsers.webPageParsers");
        Set<Class<? extends WebPageParser>> classes = reflections.getSubTypesOf(WebPageParser.class);
        for (Class c : classes) {
            try {
                WebPageParser newInstance = (WebPageParser) c.getConstructor().newInstance();
                parsers.add(newInstance);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public WebPageParser getParser(String url, String action) {
        for (WebPageParser parser : parsers) {
            if (parser.canParse(url, action)) {
                return parser;
            }
        }
        logger.warn("Failed to find a webpage parser for url=" + url + ", action=" + action);
        return new NoopParser();
    }
}
