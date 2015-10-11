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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ProductParserFactory {
    private final Set<ProductParser> parsers = new HashSet();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ProductParserFactory() {
        Reflections reflections = new Reflections("com.naxsoft.parsers.productParser", new Scanner[0]);
        Set classes = reflections.getSubTypesOf(ProductParser.class);

        for (Class clazz : (Iterable<Class>) classes) {
            try {
                ProductParser e = (ProductParser) clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
                parsers.add(e);
            } catch (Exception e) {
                logger.error("Failed to create a new product parser", e);
            }
        }
    }

    public ProductParser getParser(WebPageEntity webPageEntity) {
        Iterator it = this.parsers.iterator();

        for (ProductParser parser : parsers) {
            if (parser.canParse(webPageEntity)) {
                this.logger.debug("Found a parser " + parser.getClass().toString() + " for action = " + webPageEntity.getType() + " url = " + webPageEntity.getUrl());
                return parser;
            }
        }
        logger.warn("Failed to find a document parser for action = " + webPageEntity.getType() + " url = " + webPageEntity.getUrl());
        return new NoopParser();
    }
}
