//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.productParser;

import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ProductParserFactory {
    private Set<ProductParser> parsers = new HashSet();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public ProductParserFactory() {
        Reflections reflections = new Reflections("com.naxsoft.parsers.productParser", new Scanner[0]);
        Set classes = reflections.getSubTypesOf(ProductParser.class);

        for (Class clazz : (Iterable<Class>) classes) {
            try {
                ProductParser e = (ProductParser) clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
                this.parsers.add(e);
            } catch (Exception e) {
                this.logger.error("Failed to create a new product parser", e);
            }
        }
    }

    public ProductParser getParser(String url, String action) {
        Iterator it = this.parsers.iterator();

        ProductParser parser;
        do {
            if(!it.hasNext()) {
                this.logger.warn("Failed to find a document parser for url=" + url + ", action=" + action);
                return new NoopParser();
            }

            parser = (ProductParser)it.next();
        } while(!parser.canParse(url, action));

        return parser;
    }
}
