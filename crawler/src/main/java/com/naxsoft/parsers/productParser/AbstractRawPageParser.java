package com.naxsoft.parsers.productParser;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import io.vertx.core.AbstractVerticle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractRawPageParser extends AbstractVerticle implements ProductParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRawPageParser.class);

    @Override
    public void start() throws Exception {
        Class<? extends AbstractRawPageParser> clazz = this.getClass();
        String clazzName = clazz.getName();

        LOGGER.debug("Instantiating {}", clazzName);
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(clazz);
        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setAppend(true);
        fileAppender.setFile("logs/" + clazzName + ".log");
        fileAppender.setName(clazzName);
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
    }
}
