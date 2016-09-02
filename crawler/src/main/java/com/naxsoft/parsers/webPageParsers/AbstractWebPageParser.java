package com.naxsoft.parsers.webPageParsers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.naxsoft.crawler.AbstractCompletionHandler;
import io.vertx.core.AbstractVerticle;
import org.asynchttpclient.Response;
import org.asynchttpclient.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractWebPageParser extends AbstractVerticle implements WebPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractWebPageParser.class);

    /**
     * Return all the cookies contained in HTTP server response
     */
    private final static AbstractCompletionHandler<List<Cookie>> COOKIE_HANDLER = new AbstractCompletionHandler<List<Cookie>>() {
        @Override
        public List<Cookie> onCompleted(Response response) throws Exception {
            return response.getCookies();
        }
    };

    /**
     * @return HTTP cookie handler
     */
    protected static AbstractCompletionHandler<List<Cookie>> getCookiesHandler() {
        return COOKIE_HANDLER;
    }

    @Override
    public void start() throws Exception {
        Class<? extends AbstractWebPageParser> clazz = this.getClass();
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
