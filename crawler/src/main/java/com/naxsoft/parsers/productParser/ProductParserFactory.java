package com.naxsoft.parsers.productParser;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class ProductParserFactory {
    private Set<ProductParser> parsers = new HashSet<>();
    private final Logger logger;

    public ProductParserFactory() {
        logger = LoggerFactory.getLogger(ProductParserFactory.class);
        Reflections reflections = new Reflections("com.naxsoft.parsers.productParser");
        Set<Class<? extends ProductParser>> classes = reflections.getSubTypesOf(ProductParser.class);
        for (Class c : classes) {
            try {
                ProductParser newInstance = (ProductParser) c.getConstructor().newInstance();
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

    public ProductParser getParser(String url, String action) {
        for (ProductParser parser : parsers) {
            if (parser.canParse(url, action)) {
                return parser;
            }
        }
        logger.warn("Failed to find a document parser for url=" + url + ", action=" + action);
        return new NoopParser();
    }
}
