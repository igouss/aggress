//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.WebPageEntity;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ProductParserFactory {
    private static final Logger logger = LoggerFactory.getLogger(ProductParserFactory.class);
    private final Set<ProductParser> parsers = new HashSet<>();


    public ProductParserFactory() {
        Reflections reflections = new Reflections("com.naxsoft.parsers.productParser", new Scanner[0]);
        Set classes = reflections.getSubTypesOf(ProductParser.class);

        for (Class clazz : (Iterable<Class>) classes) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    logger.info("Instantiating " + clazz.getName());
                    ProductParser e = (ProductParser) clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
                    parsers.add(e);
                } catch (Exception e) {
                    logger.error("Failed to create a new product parser", e);
                }
            }
        }
    }

    public ProductParser getParser(WebPageEntity webPageEntity) {
        for (ProductParser parser : parsers) {
            if (parser.canParse(webPageEntity)) {
                logger.debug("Found a parser {} for action = {} url = {}", parser.getClass(), webPageEntity.getType(), webPageEntity.getUrl());
                return parser;
            }
        }
        logger.warn("Failed to find a document parser for action = {} url = {}", webPageEntity.getType(), webPageEntity.getUrl());
        return new NoopParser();
    }

}
