//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class WebPageParserFactory {
    private Set<WebPageParser> parsers = new HashSet();
    private final Logger logger = LoggerFactory.getLogger(WebPageParserFactory.class);

    public WebPageParserFactory() {
        Reflections reflections = new Reflections("com.naxsoft.parsers.webPageParsers", new Scanner[0]);
        Set classes = reflections.getSubTypesOf(WebPageParser.class);

        Iterator it = classes.iterator();
        while (it.hasNext()) {
            Class clazz = (Class) it.next();
            try {
                WebPageParser webPageParser = (WebPageParser) clazz.getConstructor(new Class[0]).newInstance(new Object[0]);
                this.parsers.add(webPageParser);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public WebPageParser getParser(String url, String action) {
        Iterator it = this.parsers.iterator();
        WebPageParser parser;
        do {
            if (!it.hasNext()) {
                this.logger.warn("Failed to find a web-page parser for url=" + url + ", action=" + action);
                return new NoopParser();
            }

            parser = (WebPageParser) it.next();
        } while (!parser.canParse(url, action));

        this.logger.debug("Found a web-page parser for url=" + url + ", action=" + action);
        return parser;
    }
}
