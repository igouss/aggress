package com.naxsoft.parsers.webPageParsers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.entity.WebPageEntity;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class WebPageParserFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebPageParserFactory.class);

    private final Set<WebPageParser> parsers = new HashSet<>();
    private final HttpClient client;

    /**
     * Create new WebPageParserFactory that using reflection to locate all WebPageParsers in the classpath
     *
     * @param client HTTP client for WebPageParsers to use
     */
    @Inject
    public WebPageParserFactory(HttpClient client) {
        this.client = client;

        Reflections reflections = new Reflections("com.naxsoft.parsers.webPageParsers");
        Set<Class<? extends WebPageParser>> classes = reflections.getSubTypesOf(WebPageParser.class);

        Class<?> asyncFetchClient = null;
        for (Class iface : client.getClass().getInterfaces()) {
            if (iface.getCanonicalName().equals("com.naxsoft.crawler.HttpClient")) {
                asyncFetchClient = iface;
            }
        }

        for (Class<? extends WebPageParser> clazz : classes) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    LOGGER.debug("Instantiating {}", clazz.getName());

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
                    fileAppender.start();
                    logger.setLevel(Level.ALL);
                    logger.addAppender(fileAppender);

                    Constructor<? extends WebPageParser> constructor = clazz.getDeclaredConstructor(asyncFetchClient);
                    constructor.setAccessible(true);

                    WebPageParser webPageParser = constructor.newInstance(client);
                    this.parsers.add(webPageParser);
                } catch (Exception e) {
                    LOGGER.error("Failed to instantiate WebPage parser {}", clazz, e);
                }
            }
        }
    }

    /**
     * Get a WebPageParser that is capable of parsing webPageEntity
     *
     * @param webPageEntity Page to parse
     * @return WebPageParser that if capable of parsing webPageEntity
     */
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        try {
            WebPageParser parserToUse = null;
            for (WebPageParser parser : parsers) {
                if (parser.canParse(webPageEntity)) {
                    LOGGER.debug("Found a parser {} for action = {} url = {}", parser.getClass(), webPageEntity.getType(), webPageEntity.getUrl());
                    parserToUse = parser;
                    break;
                }
            }
            if (parserToUse == null) {
                LOGGER.warn("Failed to find a web-page parser for action = {}, url = {}", webPageEntity.getType(), webPageEntity.getUrl());
                parserToUse = new NoopParser(client);
            }
            return parserToUse.parse(webPageEntity);
        } catch (Exception e) {
            LOGGER.error("Failed tp parse", e);
            return Observable.empty();
        }
    }
}
