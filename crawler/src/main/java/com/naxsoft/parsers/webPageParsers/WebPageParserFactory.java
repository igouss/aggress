package com.naxsoft.parsers.webPageParsers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.encoders.Encoder;
import com.naxsoft.encoders.WebPageEntityEncoder;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.utils.SitesUtil;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Copyright NAXSoft 2015
 */
public class WebPageParserFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebPageParserFactory.class);

    private final Vertx vertx;
    private final LinkedBlockingDeque<String> parserVertex;
    private final Meter parseWebPageResultsSensor;

    /**
     * Create new WebPageParserFactory that using reflection to locate all WebPageParsers in the classpath
     *
     * @param client HTTP client for WebPageParsers to use
     */
    @Inject
    public WebPageParserFactory(Vertx vertx, HttpClient client, MetricRegistry metricRegistry) {
        this.vertx = vertx;
        parserVertex = new LinkedBlockingDeque<>();

        parseWebPageResultsSensor = metricRegistry.meter("parse.webPage.results");

        vertx.eventBus().registerDefaultCodec(WebPageEntity.class, new MessageCodec<WebPageEntity, Object>() {
            @Override
            public void encodeToWire(Buffer buffer, WebPageEntity webPageEntity) {
                String jsonToStr = Encoder.encode(webPageEntity);

                // Length of JSON: is NOT characters count
                int length = jsonToStr.getBytes().length;
                // Write data into given buffer
                buffer.appendInt(length);
                buffer.appendString(jsonToStr);

            }

            @Override
            public Object decodeFromWire(int pos, Buffer buffer) {
                int _pos = pos;

                // Length of JSON
                int length = buffer.getInt(_pos);

                // Get JSON string by it`s length
                // Jump 4 because getInt() == 4 bytes
                String jsonStr = buffer.getString(_pos += 4, _pos + length);

                return WebPageEntityEncoder.decode(jsonStr);
            }

            @Override
            public Object transform(WebPageEntity webPageEntity) {
                return webPageEntity;
            }

            @Override
            public String name() {
                return "WebPageEntityCodec";
            }

            @Override
            public byte systemCodecID() {
                return -1;
            }
        });

        DeploymentOptions options = new DeploymentOptions()
                .setWorker(true)
                .setMultiThreaded(true)
                .setWorkerPoolName("webPageParsers");

        Reflections reflections = new Reflections("com.naxsoft.parsers.webPageParsers");
        Set<Class<? extends AbstractWebPageParser>> classes = reflections.getSubTypesOf(AbstractWebPageParser.class);

        classes.stream().filter(clazz -> !Modifier.isAbstract(clazz.getModifiers())).forEach(clazz -> {
            try {
                createLogger(clazz);

                Constructor<? extends AbstractWebPageParser> constructor = clazz.getDeclaredConstructor(MetricRegistry.class, HttpClient.class);
                constructor.setAccessible(true);
                AbstractWebPageParser webPageParser = constructor.newInstance(metricRegistry, client);
                vertx.deployVerticle(webPageParser, options, res -> {
                    if (res.succeeded()) {
                        LOGGER.debug("deployment id {} {}", res.result(), clazz.getName());
                        parserVertex.add(res.result());
                    } else {
                        LOGGER.error("Deployment failed!", res.cause());
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Failed to instantiate WebPage parser {}", clazz, e);
            }
        });
    }

    private void createLogger(Class<? extends AbstractWebPageParser> clazz) {
        String clazzName = clazz.getName();

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(clazz);
        LOGGER.trace("Adding new {}", logger);

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
        encoder.setContext(logger.getLoggerContext());
        encoder.setImmediateFlush(false);
        encoder.start();

        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
        fileAppender.setAppend(true);
        fileAppender.setFile("logs/" + clazzName + ".log");
        fileAppender.setName(clazzName);
        fileAppender.setContext(logger.getLoggerContext());
        fileAppender.setEncoder(encoder);
        fileAppender.start();
        logger.setLevel(Level.ALL);
        logger.addAppender(fileAppender);
    }

    /**
     * Get a WebPageParser that is capable of parsing webPageEntity
     *
     * @param webPageEntity Page to parse
     */
    public boolean parse(List<WebPageEntity> webPageEntity) {
        for (WebPageEntity entity : webPageEntity) {
            LOGGER.trace("Publishing work {}", entity);
            String host = SitesUtil.getHost(entity);
            String mailBox = host + "/" + entity.getType();
            vertx.eventBus().publish(mailBox, entity);
            parseWebPageResultsSensor.mark();
        }
        return true;
    }

    public void close() {
        parserVertex.forEach(vertx::undeploy);
    }
}
