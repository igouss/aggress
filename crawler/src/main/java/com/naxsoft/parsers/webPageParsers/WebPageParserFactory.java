//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.entity.WebPageEntity;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class WebPageParserFactory {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Set<WebPageParser> parsers = new HashSet();
    private AsyncFetchClient<Set<WebPageEntity>> client;
    public WebPageParserFactory(AsyncFetchClient<Set<WebPageEntity>> client) {

        this.client = client;

        Reflections reflections = new Reflections("com.naxsoft.parsers.webPageParsers", new Scanner[0]);
        Set classes = reflections.getSubTypesOf(WebPageParser.class);

        for (Class clazz : (Iterable<Class>) classes) {
            try {
                WebPageParser webPageParser = (WebPageParser) clazz.getConstructor(client.getClass()).newInstance(client);
                this.parsers.add(webPageParser);
            } catch (Exception e) {
                logger.error("Failed to instantiate WebPage parser " + clazz);
            }
        }

    }

    public WebPageParser getParser(WebPageEntity webPageEntity) {

        for (WebPageParser parser : parsers) {
            if (parser.canParse(webPageEntity)) {
                this.logger.debug("Found a parser " + parser.getClass().toString() + " for action = " + webPageEntity.getType() + " url = " + webPageEntity.getUrl());
                return parser;
            }
        }
        logger.warn("Failed to find a web-page parser for action =" + webPageEntity.getType() + ", url = " + webPageEntity.getUrl());
        return new NoopParser(client);
    }
}
