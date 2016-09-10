package com.naxsoft.database;


import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.event.metrics.CommandLatencyEvent;
import com.lambdaworks.redis.metrics.DefaultCommandLatencyCollectorOptions;
import com.lambdaworks.redis.resource.ClientResources;
import com.lambdaworks.redis.resource.DefaultClientResources;
import com.naxsoft.encoders.ProductEntityEncoder;
import com.naxsoft.encoders.WebPageEntityEncoder;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
import com.naxsoft.utils.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RedisDatabase implements Persistent {
    private final static Logger LOGGER = LoggerFactory.getLogger(RedisDatabase.class);
    private static final Long BATCH_SIZE = 20L;
    @Inject
    ProductEntityEncoder productEntityEncoder;
    @Inject
    WebPageEntityEncoder webPageEntityEncoder;
    private ClientResources res;
    private RedisClient redisClient;
    //    private StatefulRedisPubSubConnection<String, String> pubSub;
//    private RedisConnectionPool<RedisAsyncCommands<String, String>> pool;
    private StatefulRedisConnection<String, String> connection;

    public RedisDatabase() throws PropertyNotFoundException {
        this(AppProperties.getProperty("redisHost").getValue(), Integer.parseInt(AppProperties.getProperty("redisPort").getValue()));
    }

    private RedisDatabase(String host, int port) {
        res = DefaultClientResources
                .builder()
                .commandLatencyCollectorOptions(DefaultCommandLatencyCollectorOptions.create())
                .build();
        redisClient = RedisClient.create(res, RedisURI.Builder.redis(host, port).build());
        redisClient.getResources().eventBus().get()
                .filter(redisEvent -> redisEvent instanceof CommandLatencyEvent)
                .cast(CommandLatencyEvent.class)
                .subscribeOn(Schedulers.trampoline())
                .subscribe(
                        e -> LOGGER.info(e.getLatencies().toString()),
                        err -> LOGGER.error("Failed to get command latency", err),
                        () -> LOGGER.info("Command latency complete")
                );
//        pubSub = redisClient.connectPubSub();
//        pool = redisClient.asyncPool();
        connection = redisClient.connect();
    }

    public void setProductEntityEncoder(ProductEntityEncoder productEntityEncoder) {
        this.productEntityEncoder = productEntityEncoder;
    }

    public void setWebPageEntityEncoder(WebPageEntityEncoder webPageEntityEncoder) {
        this.webPageEntityEncoder = webPageEntityEncoder;
    }

    @Override
    public void close() {
        redisClient.shutdown();
        res.shutdown();
    }

    @Override
    public Observable<Long> getUnparsedCount(String type) {
        return connection.reactive().scard("WebPageEntity." + type);
    }

    @Override
    public Observable<Long> markWebPageAsParsed(WebPageEntity webPageEntity) {
        if (webPageEntity == null) {
            return Observable.error(new Exception("Trying to mark null WebPageEntity as parsed"));
        }

        String source = "WebPageEntity." + webPageEntity.getType();
        String destination = "WebPageEntity." + webPageEntity.getType() + ".parsed";
        String member = webPageEntityEncoder.encode(webPageEntity);
//        return connection.reactive()
//                .sadd(destination, member);
        return connection.reactive()
                .sadd(destination, member)
                .map(res -> {
                    if (res != 0L) {
                        LOGGER.trace("Moved element {} from {} to {}", member, source, destination);
                        return 1L;
                    } else {
                        LOGGER.info("Failed to move element {} from {} to {}", member, source, destination);
                        return 0L;
                    }
                });
    }

    @Override
    public Observable<Integer> markAllProductPagesAsIndexed() {
        return null;
    }

    @Override
    public Observable<Long> addProductPageEntry(Observable<ProductEntity> productEntity) {
        return productEntity
                .flatMap(entity -> {
                    String key = "ProductEntity";
                    String member = productEntityEncoder.encode(entity);
                    return Observable.zip(connection.reactive().sadd(key, member), Observable.just(entity), Tuple::new);
                })
                .map(res -> {
                    if (res.getV1() == 0L) {
                        LOGGER.error("Failed to save {}", res.getV2());
                    } else {
                        LOGGER.trace("Saved {}", res.getV2());
                    }
                    return res.getV1();
                });
    }

    @Override
    public Observable<Long> addWebPageEntry(Observable<WebPageEntity> webPageEntity) {
        return webPageEntity
                .flatMap(entity -> {
                    String key = "WebPageEntity" + "." + entity.getType();
                    String member = webPageEntityEncoder.encode(entity);
                    return Observable.zip(connection.reactive().sadd(key, member), Observable.just(entity), Tuple::new);
                })
                .map(res -> {
                    if (res.getV1() == 0L) {
                        LOGGER.error("Failed to save {}", res.getV2());
                    } else {
                        LOGGER.trace("Saved {}", res.getV2());
                    }
                    return res.getV1();
                });
    }

    @Override
    public Observable<ProductEntity> getProducts() {
        return connection.reactive()
                .smembers("ProductEntity")
                .map(productEntityEncoder::decode)
                .filter(productEntity -> productEntity != null);
    }

    @Override
    public Observable<WebPageEntity> getUnparsedByType(String type, Long count) {
        LOGGER.info("getUnparsedByType {} {}", type, count);
        String key = "WebPageEntity." + type;
        long popCount = Math.min(BATCH_SIZE, count);
//        return connection.reactive().spop(key, popCount).map(webPageEntityEncoder::decode);
        return connection.reactive()
                .spop(key, popCount)
                .map(webPageEntityEncoder::decode)
                .filter(entry -> entry != null);
    }

    @Override
    public Observable<Long> cleanUp(String[] tables) {
        return connection.reactive().del(
                "WebPageEntity.frontPage",
                "WebPageEntity.frontPage.parsed",
                "WebPageEntity.productList",
                "WebPageEntity.productList.parsed",
                "WebPageEntity.productPage",
                "WebPageEntity.productPage.parsed",
                "ProductEntity"
        );
    }
}
