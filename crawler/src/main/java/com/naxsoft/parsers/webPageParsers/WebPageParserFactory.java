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
import io.vertx.core.eventbus.MessageConsumer;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Emitter;
import rx.Observable;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Copyright NAXSoft 2015
 */
public class WebPageParserFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebPageParserFactory.class);

    private final Vertx vertx;
    private final LinkedBlockingDeque<String> parserVertex;
    private final Observable<WebPageEntity> parseResult;
    private final Meter parseWebPageRequestsSensor;
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

        parseWebPageRequestsSensor = metricRegistry.meter("parse.webPage.requests");
        parseWebPageResultsSensor = metricRegistry.meter("parse.webPage.results");

        MessageConsumer<WebPageEntity> consumer = vertx.eventBus().consumer("webPageParseResult");

        parseResult = Observable.fromEmitter(asyncEmitter -> {
            consumer.handler(handler -> asyncEmitter.onNext(handler.body()));
            consumer.endHandler(v -> asyncEmitter.onCompleted());
        }, Emitter.BackpressureMode.BUFFER);

//        parseResult = Observable.fromAsync(asyncEmitter -> {
//            vertx.eventBus().consumer("webPageParseResult", event -> asyncEmitter.onNext((WebPageEntity) event.body()));
//        }, AsyncEmitter.BackpressureMode.ERROR);

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

        DeploymentOptions options = new DeploymentOptions().setWorker(true);

        Reflections reflections = new Reflections("com.naxsoft.parsers.webPageParsers");
        Set<Class<? extends AbstractWebPageParser>> classes = reflections.getSubTypesOf(AbstractWebPageParser.class);

        final Class<?> asyncFetchClient = HttpClient.class;
//        for (Class iface : client.getClass().getInterfaces()) {
//            if (iface.getCanonicalName().equals("com.naxsoft.crawler.HttpClient")) {
//                asyncFetchClient = iface;
//            }
//        }

        Observable.fromEmitter((Action1<Emitter<Class>>) asyncEmitter -> {
            classes.stream().filter(clazz -> !Modifier.isAbstract(clazz.getModifiers())).forEach(clazz -> {
                try {
                    createLogger(clazz);

                    Constructor<? extends AbstractWebPageParser> constructor = clazz.getDeclaredConstructor(asyncFetchClient);
                    constructor.setAccessible(true);
                    AbstractWebPageParser webPageParser = constructor.newInstance(client);
                    vertx.deployVerticle(webPageParser, options, res -> {
                        if (res.succeeded()) {
                            LOGGER.debug("deployment id {} {}", res.result(), clazz.getName());
                            parserVertex.add(res.result());
                            asyncEmitter.onNext(clazz);
                        } else {
                            LOGGER.error("Deployment failed!", res.cause());
                        }
                    });
                } catch (Exception e) {
                    LOGGER.error("Failed to instantiate WebPage parser {}", clazz, e);
                }
            });
            asyncEmitter.onCompleted();
        }, Emitter.BackpressureMode.BUFFER).subscribeOn(Schedulers.immediate()).subscribe();


    }

    private void createLogger(Class<? extends AbstractWebPageParser> clazz) {
        String clazzName = clazz.getName();

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

    /**
     * Get a WebPageParser that is capable of parsing webPageEntity
     *
     * @param webPageEntity Page to parse
     */
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        parseWebPageRequestsSensor.mark();

        String host = SitesUtil.getHost(webPageEntity);
        String mailBox = host + "/" + webPageEntity.getType();
        vertx.eventBus().publish(mailBox, webPageEntity);
        return parseResult.doOnNext(val -> parseWebPageResultsSensor.mark());
    }

    public void close() {
        parserVertex.forEach(vertx::undeploy);
    }
}
