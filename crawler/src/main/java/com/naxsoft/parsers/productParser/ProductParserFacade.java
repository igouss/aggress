package com.naxsoft.parsers.productParser;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class ProductParserFacade {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductParserFacade.class);
    private final Set<ProductParser> parsers = new HashSet<>();

    public ProductParserFacade() {
        Reflections reflections = new Reflections("com.naxsoft.parsers.productParser");
        Set<Class<? extends ProductParser>> classes = reflections.getSubTypesOf(ProductParser.class);

        for (Class<? extends ProductParser> clazz : classes) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    LOGGER.info("Instantiating {}", clazz.getName());

                    ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(clazz);
                    FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
                    fileAppender.setAppend(true);
                    fileAppender.setFile("logs/" + clazz.getName() + ".log");
                    fileAppender.setName(clazz.getName());
                    fileAppender.setContext(logger.getLoggerContext());
                    PatternLayoutEncoder encoder = new PatternLayoutEncoder();
                    encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
                    encoder.setContext(logger.getLoggerContext());
                    encoder.setImmediateFlush(false);
                    encoder.start();
                    fileAppender.setEncoder(encoder);
                    logger.setLevel(Level.ALL);

                    logger.addAppender(fileAppender);


                    Constructor<? extends ProductParser> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);

                    ProductParser e = constructor.newInstance();
                    if (!(e instanceof NoopParser)) {
                        parsers.add(e);
                    }
                } catch (Exception e) {
                    LOGGER.error("Failed to create a new product parser", e);
                }
            }
        }
    }

    /**
     * Get ProductParser capable of parsing webPageEntity
     *
     * @param webPageEntity page to parse
     * @return Parser capable of parsing the page
     */
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws ProductParseException {
        try {
            ProductParser parserToUse = null;
            for (ProductParser parser : parsers) {
                if (parser.canParse(webPageEntity)) {
                    LOGGER.debug("Found a parser {} for action = {} url = {}", parser.getClass(), webPageEntity.getType(), webPageEntity.getUrl());
                    parserToUse = parser;
                    break;
                }
            }
            if (parserToUse == null) {
                LOGGER.warn("Failed to find a document parser for action = {} url = {}", webPageEntity.getType(), webPageEntity.getUrl());
                parserToUse = new NoopParser();
            }
            return parserToUse.parse(webPageEntity);
        } catch (Exception e) {
            LOGGER.error("Failed tp parse", e);
            return Collections.emptySet();
        }
    }
}
