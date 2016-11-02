package com.naxsoft.parsers.productParser;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.naxsoft.encoders.Encoder;
import com.naxsoft.encoders.ProductEntityEncoder;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.utils.SitesUtil;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.MessageConsumer;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
public class ProductParserFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductParserFactory.class);

    private final Vertx vertx;
    private final ArrayList<String> parserVertex;
    private final Flowable<ProductEntity> parseResult;

    private final Meter parseWebPageRawRequestsSensor;
    private final Meter parseProductResultSensor;

    @Inject
    public ProductParserFactory(Vertx vertx, MetricRegistry metricRegistry) {
        this.vertx = vertx;
        parserVertex = new ArrayList<>();

        parseWebPageRawRequestsSensor = metricRegistry.meter("parse.webPage.raw.requests");
        parseProductResultSensor = metricRegistry.meter("parse.Product.result");

        vertx.eventBus().registerDefaultCodec(ProductEntity.class, new MessageCodec<ProductEntity, Object>() {
            @Override
            public void encodeToWire(Buffer buffer, ProductEntity productEntity) {
                String jsonToStr = Encoder.encode(productEntity);

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

                return ProductEntityEncoder.decode(jsonStr);
            }

            @Override
            public Object transform(ProductEntity webPageEntity) {
                return webPageEntity;
            }

            @Override
            public String name() {
                return "ProductEntityCodec";
            }

            @Override
            public byte systemCodecID() {
                return -1;
            }
        });

        DeploymentOptions options = new DeploymentOptions()
                .setWorker(true)
                .setMultiThreaded(true)
                .setWorkerPoolName("productParser");

        Reflections reflections = new Reflections("com.naxsoft.parsers.productParser");
        Set<Class<? extends AbstractRawPageParser>> classes = reflections.getSubTypesOf(AbstractRawPageParser.class);

        classes.stream()
                .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
                .forEach(clazz -> {
                    try {
                        createLogger(clazz);

                        Constructor<? extends AbstractRawPageParser> constructor = clazz.getDeclaredConstructor(MetricRegistry.class);
                        constructor.setAccessible(true);

                        AbstractRawPageParser productParser = constructor.newInstance(metricRegistry);
                        vertx.deployVerticle(productParser, options, res -> {
                            if (res.succeeded()) {
                                LOGGER.debug("deployment id {} {}", res.result(), clazz.getName());
                                parserVertex.add(res.result());
                            } else {
                                LOGGER.error("Deployment failed!", res.cause());
                            }
                        });
                    } catch (Exception e) {
                        LOGGER.error("Failed to create a new product parser", e);
                    }
                });

        MessageConsumer<ProductEntity> consumer = vertx.eventBus().consumer("productParseResult");
        parseResult = Flowable.create((FlowableEmitter<ProductEntity> emitter) -> {
            consumer.handler(handler -> emitter.onNext(handler.body()));
            consumer.endHandler(v -> emitter.onComplete());
        }, BackpressureStrategy.BUFFER);
    }

    private void createLogger(Class<? extends AbstractRawPageParser> clazz) {
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
     * Get ProductParser capable of parsing webPageEntity
     *
     * @param webPageEntity page to parse
     * @return Parser capable of parsing the page
     */
    public Flowable<ProductEntity> parse(WebPageEntity webPageEntity) {
        parseWebPageRawRequestsSensor.mark();

        String host = SitesUtil.getHost(webPageEntity);
        String type = webPageEntity.getType();
        String mailbox = host + "/" + type;
        LOGGER.info("Sending to mailbox {} value {}", mailbox, webPageEntity);
        vertx.eventBus().publish(mailbox, webPageEntity);
        return parseResult.doOnNext(val -> {
            LOGGER.info("Product parse results {}", val);
            parseProductResultSensor.mark();
        });
    }

    public void close() {
        parserVertex.forEach(vertx::undeploy);
    }
}
