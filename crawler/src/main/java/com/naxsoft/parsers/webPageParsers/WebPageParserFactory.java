//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.webPageParsers;

import com.naxsoft.parsers.webPageParsers.NoopParser;
import com.naxsoft.parsers.webPageParsers.WebPageParser;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebPageParserFactory {
    private Set<WebPageParser> parsers = new HashSet();
    private final Logger logger = LoggerFactory.getLogger(WebPageParserFactory.class);

    public WebPageParserFactory() {
        Reflections reflections = new Reflections("com.naxsoft.parsers.webPageParsers", new Scanner[0]);
        Set classes = reflections.getSubTypesOf(WebPageParser.class);
        Iterator var3 = classes.iterator();

        while(var3.hasNext()) {
            Class c = (Class)var3.next();

            try {
                WebPageParser e = (WebPageParser)c.getConstructor(new Class[0]).newInstance(new Object[0]);
                this.parsers.add(e);
            } catch (NoSuchMethodException var6) {
                var6.printStackTrace();
            } catch (InvocationTargetException var7) {
                var7.printStackTrace();
            } catch (InstantiationException var8) {
                var8.printStackTrace();
            } catch (IllegalAccessException var9) {
                var9.printStackTrace();
            }
        }

    }

    public WebPageParser getParser(String url, String action) {
        Iterator var3 = this.parsers.iterator();

        WebPageParser parser;
        do {
            if(!var3.hasNext()) {
                this.logger.warn("Failed to find a web-page parser for url=" + url + ", action=" + action);
                return new NoopParser();
            }

            parser = (WebPageParser)var3.next();
        } while(!parser.canParse(url, action));

        this.logger.debug("Found a web-page parser for url=" + url + ", action=" + action);
        return parser;
    }
}
