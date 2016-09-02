package com.naxsoft.parsers.productParser;

import com.naxsoft.encoders.ProductEntityEncoder;
import com.naxsoft.entity.ProductEntity;
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
                String jsonStr = buffer.getString(_pos += 4, _pos += length);

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
                    LOGGER.info("Instantiating {}", clazz.getName());

                    Constructor<? extends AbstractRawPageParser> constructor = clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);

                    AbstractRawPageParser productParser = constructor.newInstance();
                    vertx.deployVerticle(productParser, options, res -> {
                        if (res.succeeded()) {
                            System.out.println("Deployment id is: " + res.result());
                            parserVertex.add(res.result());
                        } else {
                            System.out.println("Deployment failed!");
                        }
                    });
                } catch (Exception e) {
                    LOGGER.error("Failed to create a new product parser", e);
                }
            }
        }
        parseResult = Observable.fromAsync(asyncEmitter -> vertx.eventBus().consumer("productParseResult", (Handler<Message<ProductEntity>>) event -> asyncEmitter.onNext(event.body())), AsyncEmitter.BackpressureMode.BUFFER);
    }

    /**
     * Get ProductParser capable of parsing webPageEntity
     *
     * @param webPageEntity page to parse
     * @return Parser capable of parsing the page
     */
    public Observable<ProductEntity> parse(WebPageEntity webPageEntity) {
        String host = getHost(webPageEntity);
        vertx.eventBus().publish(host + "/" + webPageEntity.getType(), webPageEntity);
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
