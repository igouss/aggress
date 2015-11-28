//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.parsers.productParser;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.naxsoft.database.ObservableQuery;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.reflections.Reflections;
import org.reflections.scanners.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ProductParserFactory {
    private final Set<ProductParser> parsers = new HashSet<>();
    private static final Logger logger = LoggerFactory.getLogger(ProductParserFactory.class);
    private final MetricRegistry metricRegistry;

    public ProductParserFactory(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
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

    private ProductParser getParser(WebPageEntity webPageEntity) {
        for (ProductParser parser : parsers) {
            if (parser.canParse(webPageEntity)) {
                logger.debug("Found a parser {} for action = {} url = {}", parser.getClass(), webPageEntity.getType(), webPageEntity.getUrl());
                return parser;
            }
        }
        logger.warn("Failed to find a document parser for action = {} url = {}", webPageEntity.getType(), webPageEntity.getUrl());
        return new NoopParser();
    }

    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        ProductParser parser = getParser(webPageEntity);
        Timer parseTime = metricRegistry.timer(MetricRegistry.name(parser.getClass(), "parseTime"));
        Timer.Context time = parseTime.time();
        Set<ProductEntity> result = parser.parse(webPageEntity);
        time.stop();
        return result;

    }
}
