package com.naxsoft.parsers.webPageParsers;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import com.naxsoft.crawler.HttpClient;
import com.naxsoft.encoders.WebPageEntityEncoder;
import com.naxsoft.entity.WebPageEntity;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageCodec;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.AsyncEmitter;
import rx.Observable;

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
    private Observable<WebPageEntity> parseResult;

    /**
     * Create new WebPageParserFactory that using reflection to locate all WebPageParsers in the classpath
     *
     * @param client HTTP client for WebPageParsers to use
     */
    @Inject
    public WebPageParserFactory(Vertx vertx, HttpClient client) {
        this.vertx = vertx;
        parserVertex = new LinkedBlockingDeque<>();
        vertx.eventBus().registerDefaultCodec(WebPageEntity.class, new MessageCodec<WebPageEntity, Object>() {
            WebPageEntityEncoder webPageEntityEncoder = new WebPageEntityEncoder();

            @Override
            public void encodeToWire(Buffer buffer, WebPageEntity webPageEntity) {
                String jsonToStr = webPageEntityEncoder.encode(webPageEntity);

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
                String jsonStr = buffer.getString(_pos += 4, _pos += length);

                return webPageEntityEncoder.decode(jsonStr);
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

        Class<?> asyncFetchClient = null;
        for (Class iface : client.getClass().getInterfaces()) {
            if (iface.getCanonicalName().equals("com.naxsoft.crawler.HttpClient")) {
                asyncFetchClient = iface;
            }
        }

        for (Class<? extends AbstractWebPageParser> clazz : classes) {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                try {
                    createLogger(clazz);

                    Constructor<? extends AbstractWebPageParser> constructor = clazz.getDeclaredConstructor(asyncFetchClient);
                    constructor.setAccessible(true);
                    AbstractWebPageParser webPageParser = constructor.newInstance(client);
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
            }
        }
        parseResult = Observable.fromAsync(asyncEmitter -> vertx.eventBus().consumer("webPageParseResult", (Handler<Message<WebPageEntity>>) event -> asyncEmitter.onNext(event.body())), AsyncEmitter.BackpressureMode.BUFFER);
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

    public void close() {
        parserVertex.forEach(vertx::undeploy);
    }

    /**
     * Get a WebPageParser that is capable of parsing webPageEntity
     *
     * @param webPageEntity Page to parse
     */
    public Observable<WebPageEntity> parse(WebPageEntity webPageEntity) {
        String host = getHost(webPageEntity);
        vertx.eventBus().publish(host + "/" + webPageEntity.getType(), webPageEntity);
        return parseResult;
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
        } else if (url.contains("")) {
            host = "gun-shop.ca";
        } else if (url.contains("")) {
            host = "gun-shop.ca";
        } else if (url.contains("hical.ca")) {
            host = "hical.ca";
        } else if (url.contains("internationalshootingsupplies.com")) {
            host = "internationalshootingsupplies.com";
        } else if (url.contains("irunguns.us")) {
            host = "irunguns.us";
        } else if (url.contains("")) {
            host = "leverarms.com";
        } else if (url.contains("leverarms.com")) {
            host = "magnumguns.ca";
        } else if (url.contains("magnumguns.ca")) {
            host = "marstar.ca";
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
