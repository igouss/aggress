package com.naxsoft.parsers.productParser;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.naxsoft.encoders.ProductEntityEncoder;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.MessageConsumer;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.AsyncEmitter;
import rx.Observable;

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
    private Observable<ProductEntity> parseResult;

    @Inject
    public ProductParserFactory(Vertx vertx) {
        this.vertx = vertx;
        parserVertex = new ArrayList<>();
        vertx.eventBus().registerDefaultCodec(ProductEntity.class, new MessageCodec<ProductEntity, Object>() {
            ProductEntityEncoder entityEncoder = new ProductEntityEncoder();

            @Override
            public void encodeToWire(Buffer buffer, ProductEntity productEntity) {
                String jsonToStr = entityEncoder.encode(productEntity);

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

                return entityEncoder.decode(jsonStr);
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

        DeploymentOptions options = new DeploymentOptions().setWorker(true);

        Reflections reflections = new Reflections("com.naxsoft.parsers.productParser");
        Set<Class<? extends AbstractRawPageParser>> classes = reflections.getSubTypesOf(AbstractRawPageParser.class);

        for (Class<? extends AbstractRawPageParser> clazz : classes) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    createLogger(clazz);

                    Constructor<? extends AbstractRawPageParser> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);

                    AbstractRawPageParser productParser = constructor.newInstance();
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
            }
        }

        MessageConsumer<ProductEntity> consumer = vertx.eventBus().consumer("productParseResult");
        parseResult = Observable.fromAsync(asyncEmitter -> {
            consumer.handler(handler -> asyncEmitter.onNext(handler.body()));
            consumer.endHandler(v -> asyncEmitter.onCompleted());
        }, AsyncEmitter.BackpressureMode.BUFFER);
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
    public Observable<ProductEntity> parse(WebPageEntity webPageEntity) {
        String host = getHost(webPageEntity);
        String type = webPageEntity.getType();
        String mailbox = host + "/" + type;
        assert type.equals("productPageRaw");
        vertx.eventBus().publish(mailbox, webPageEntity);
        return parseResult;
    }

    public void close() {
        parserVertex.forEach(vertx::undeploy);
    }

    private String getHost(WebPageEntity webPageEntity) {
        String url = webPageEntity.getUrl();
        String host = "noopWebPageParser";
        if (url.contains("alflahertys.com")) {
            host = "alflahertys.com";
        } else if (url.contains("bullseyelondon.com")) {
            host = "bullseyelondon.com";
        } else if (url.contains("cabelas.ca")) {
            host = "cabelas.ca";
        } else if (url.contains("canadaammo.com")) {
            host = "canadaammo.com";
        } else if (url.contains("canadiangunnutz.com")) {
            host = "canadiangunnutz.com";
        } else if (url.contains("corwin-arms.com")) {
            host = "corwin-arms.com";
        } else if (url.contains("crafm.com")) {
            host = "crafm.com";
        } else if (url.contains("ctcsupplies.ca")) {
            host = "ctcsupplies.ca";
        } else if (url.contains("ctcsupplies.ca")) {
            host = "ctcsupplies.ca";
        } else if (url.contains("dantesports.com")) {
            host = "dantesports.com";
        } else if (url.contains("ellwoodepps.com")) {
            host = "ellwoodepps.com";
        } else if (url.contains("firearmsoutletcanada.com")) {
            host = "firearmsoutletcanada.com";
        } else if (url.contains("fishingworld.ca")) {
            host = "fishingworld.ca";
        } else if (url.contains("frontierfirearms.ca")) {
            host = "frontierfirearms.ca";
        } else if (url.contains("gotenda.com")) {
            host = "gotenda.com";
        } else if (url.contains("gun-shop.ca")) {
            host = "gun-shop.ca";
        } else if (url.contains("hical.ca")) {
            host = "hical.ca";
        } else if (url.contains("internationalshootingsupplies.com")) {
            host = "internationalshootingsupplies.com";
        } else if (url.contains("irunguns.us")) {
            host = "irunguns.us";
        } else if (url.contains("leverarms.com")) {
            host = "leverarms.com";
        } else if (url.contains("magnumguns.ca")) {
            host = "magnumguns.ca";
        } else if (url.contains("marstar.ca")) {
            host = "marstar.ca";
        } else if (url.contains("prophetriver.com")) {
            host = "prophetriver.com";
        } else if (url.contains("psmilitaria.50megs.com")) {
            host = "psmilitaria.50megs.com";
        } else if (url.contains("shopquestar.com")) {
            host = "shopquestar.com";
        } else if (url.contains("sail.ca")) {
            host = "sail.ca";
        } else if (url.contains("theammosource.com")) {
            host = "theammosource.com";
        } else if (url.contains("tradeexcanada.com")) {
            host = "tradeexcanada.com";
        } else if (url.contains("wanstallsonline.com")) {
            host = "wanstallsonline.com";
        } else if (url.contains("westrifle.com")) {
            host = "westrifle.com";
        } else if (url.contains("wholesalesports.com")) {
            host = "wholesalesports.com";
        } else if (url.contains("wolverinesupplies.com")) {
            host = "wolverinesupplies.com";
        }
        return host;
    }
}
