package com.naxsoft.storage.redis;


import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.event.metrics.CommandLatencyEvent;
import com.lambdaworks.redis.metrics.DefaultCommandLatencyCollectorOptions;
import com.lambdaworks.redis.resource.ClientResources;
import com.lambdaworks.redis.resource.DefaultClientResources;
import com.naxsoft.encoders.Encoder;
import com.naxsoft.encoders.ProductEntityEncoder;
import com.naxsoft.encoders.WebPageEntityEncoder;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.storage.Persistent;
import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.schedulers.Schedulers;

import javax.inject.Singleton;
import java.util.List;
import java.util.Objects;

@Singleton
public class RedisDatabase implements Persistent {
    private final static Logger LOGGER = LoggerFactory.getLogger(RedisDatabase.class);
    private static final int BATCH_SIZE = 20;

    private final RedisClient redisClient;
    //    private StatefulRedisPubSubConnection<String, String> pubSub;
//    private RedisConnectionPool<RedisAsyncCommands<String, String>> pool;
    private final StatefulRedisConnection<String, String> connection;

    public RedisDatabase() throws PropertyNotFoundException {
        this(AppProperties.getProperty("redisHost"), Integer.parseInt(AppProperties.getProperty("redisPort")));
    }

    private RedisDatabase(String host, int port) {
        ClientResources res = DefaultClientResources.builder()
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

    @Override
    public void close() {
        LOGGER.info("Shutting down redis connection");
        redisClient.shutdown();
    }

    @Override
    public Flowable<Long> markWebPageAsParsed(List<WebPageEntity> webPageEntity) {

        if (webPageEntity == null || webPageEntity.size() == 0) {
            return Flowable.just(0L);
        }

        String source = "WebPageEntity." + webPageEntity.get(0).getType();
        String destination = "WebPageEntity." + webPageEntity.get(0).getType() + ".parsed";


        String[] jsonValues = webPageEntity.stream().map((entity) -> {
            LOGGER.info("Adding {} to parsed", entity);
            return Encoder.encode(entity);
        }).toArray(String[]::new);
        return Flowable.fromFuture(connection.async().sadd(destination, jsonValues));
        //.doOnNext(res -> LOGGER.info("Moved rc={} from {} to {} element {}...", res, source, destination, member.substring(0, 50)));
    }

    @Override
    public Flowable<Integer> markAllProductPagesAsIndexed() {
        return null;
    }

    @Override
    public Flowable<Long> addProductPageEntry(List<ProductEntity> productEntity) {
        if (productEntity == null || productEntity.size() == 0) {
            return Flowable.just(0L);
        }

        String key = "ProductEntity";
        String[] jsonValues = productEntity.stream().map(Encoder::encode).toArray(String[]::new);
        return Flowable.fromFuture(connection.async().sadd(key, jsonValues));
    }

    @Override
    public Flowable<Long> addWebPageEntry(List<WebPageEntity> webPageEntity) {
        if (webPageEntity == null || webPageEntity.size() == 0) {
            return Flowable.just(0L);
        }

        String key = "WebPageEntity" + "." + webPageEntity.get(0).getType();
        String[] jsonValues = webPageEntity.stream().map(Encoder::encode).toArray(String[]::new);
        LOGGER.trace("adding key {} val {}", key, webPageEntity.get(0).getUrl());
        return Flowable.fromFuture(connection.async().sadd(key, jsonValues));
    }

    @Override
    public Flowable<ProductEntity> getProducts() {
        return Flowable.fromFuture(connection.async().smembers("ProductEntity"))
                .flatMap(Flowable::fromIterable)
                .map(ProductEntityEncoder::decode)
                .filter(Objects::nonNull);
    }

    @Override
    public Flowable<Long> getUnparsedCount(String type) {
        return Flowable.fromFuture(connection.async().scard("WebPageEntity." + type));
    }

    @Override
    public Flowable<WebPageEntity> getUnparsedByType(String type, Long count) {
        LOGGER.info("getUnparsedByType {} {}", type, count);
        return Flowable.fromFuture(connection.async().spop("WebPageEntity." + type, Math.min(count, BATCH_SIZE)))
                .flatMap(Flowable::fromIterable)
                .map(WebPageEntityEncoder::decode)
                .doOnNext(val -> LOGGER.info("SPOP'ed {} {} {}", val.getType(), val.getUrl(), val.getCategory()));
    }

    @Override
    public Flowable<String> cleanUp(String[] tables) {
        return Flowable.fromFuture(connection.async().flushall());
    }
}
