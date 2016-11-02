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
import java.util.Set;

@Singleton
public class RedisDatabase implements Persistent {
    private final static Logger LOGGER = LoggerFactory.getLogger(RedisDatabase.class);
    private static final int BATCH_SIZE = 20;

    private final ClientResources res;
    private final RedisClient redisClient;
    //    private StatefulRedisPubSubConnection<String, String> pubSub;
//    private RedisConnectionPool<RedisAsyncCommands<String, String>> pool;
    private final StatefulRedisConnection<String, String> connection;

    public RedisDatabase() throws PropertyNotFoundException {
        this(AppProperties.getProperty("redisHost"), Integer.parseInt(AppProperties.getProperty("redisPort")));
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

    @Override
    public void close() {
        redisClient.shutdown();
        res.shutdown();
    }

    @Override
    public Flowable<Long> markWebPageAsParsed(WebPageEntity webPageEntity) {
        if (webPageEntity == null) {
            return Flowable.error(new Exception("Trying to mark null WebPageEntity as parsed"));
        }

        String source = "WebPageEntity." + webPageEntity.getType();
        String destination = "WebPageEntity." + webPageEntity.getType() + ".parsed";
        String member = Encoder.encode(webPageEntity);
        return Flowable.fromFuture(connection.async().sadd(destination, member));
        //.doOnNext(res -> LOGGER.info("Moved rc={} from {} to {} element {}...", res, source, destination, member.substring(0, 50)));
    }

    @Override
    public Flowable<Integer> markAllProductPagesAsIndexed() {
        return null;
    }

    @Override
    public Flowable<Long> addProductPageEntry(ProductEntity productEntity) {
        String key = "ProductEntity";
        String member = Encoder.encode(productEntity);
        return Flowable.fromFuture(connection.async().sadd(key, member));
    }

    @Override
    public Flowable<Long> addWebPageEntry(WebPageEntity webPageEntity) {
        String key = "WebPageEntity" + "." + webPageEntity.getType();
        String member = Encoder.encode(webPageEntity);
        LOGGER.trace("adding key {} val {}", key, webPageEntity.getUrl());
        return Flowable.fromFuture(connection.async().sadd(key, member));
    }

    @Override
    public Flowable<ProductEntity> getProducts() {
        return Flowable.fromFuture(connection.async().smembers("ProductEntity"))
                .flatMap(Flowable::fromIterable)
                .map(ProductEntityEncoder::decode)
                .filter(productEntity -> productEntity != null);
    }

    @Override
    public Flowable<Long> getUnparsedCount(String type) {
        return Flowable.fromFuture(connection.async().scard("WebPageEntity." + type));
    }

    @Override
    public Flowable<WebPageEntity> getUnparsedByType(String type, Long count) {
        LOGGER.info("getUnparsedByType {} {}", type, count);
        Flowable<Set<String>> setFlowable = Flowable.fromFuture(connection.async()
                .spop("WebPageEntity." + type, Math.min(count, BATCH_SIZE)));

        return setFlowable.flatMap(Flowable::fromIterable).map(WebPageEntityEncoder::decode)
                .doOnNext(val -> LOGGER.info("SPOP'ed {} {} {}", val.getType(), val.getUrl(), val.getCategory()))
                .filter(entry -> entry != null);
    }

    @Override
    public Flowable<String> cleanUp(String[] tables) {
        return Flowable.fromFuture(connection.async().flushall());
    }
}
