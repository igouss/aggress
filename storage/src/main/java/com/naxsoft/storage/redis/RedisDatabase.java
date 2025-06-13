package com.naxsoft.storage.redis;


import com.naxsoft.encoders.Encoder;
import com.naxsoft.encoders.ProductEntityEncoder;
import com.naxsoft.encoders.WebPageEntityEncoder;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.storage.Persistent;
import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class RedisDatabase implements Persistent {
    private final static Logger LOGGER = LoggerFactory.getLogger(RedisDatabase.class);
    private static final int BATCH_SIZE = 20;

    private final ClientResources res;
    private final RedisClient redisClient;
    //    private StatefulRedisPubSubConnection<String, String> pubSub;
//    private RedisConnectionPool<RedisAsyncCommands<String, String>> pool;
    private final StatefulRedisConnection<String, String> connection;

    // Direct Reactor usage - no conversion needed

    public RedisDatabase() throws PropertyNotFoundException {
        this(AppProperties.getProperty("redisHost"), Integer.parseInt(AppProperties.getProperty("redisPort")));
    }

    private RedisDatabase(String host, int port) {
        res = DefaultClientResources
                .builder()
                .build();
        redisClient = RedisClient.create(res, RedisURI.Builder.redis(host, port).build());
        // TODO: Update event bus subscription for new Lettuce API
        // redisClient.getResources().eventBus().get()
        //         .filter(redisEvent -> redisEvent instanceof CommandLatencyEvent)
        //         .cast(CommandLatencyEvent.class)
        //         .subscribeOn(Schedulers.computation())
        //         .subscribe(
        //                 e -> LOGGER.info(e.getLatencies().toString()),
        //                 err -> LOGGER.error("Failed to get command latency", err),
        //                 () -> LOGGER.info("Command latency complete")
        //         );
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
    public Mono<Long> markWebPageAsParsed(WebPageEntity webPageEntity) {
        String source = "WebPageEntity." + webPageEntity.getType();
        String destination = "WebPageEntity." + webPageEntity.getType() + ".parsed";
        String member = Encoder.encode(webPageEntity);
        return connection.reactive().sadd(destination, member);
        //.doOnNext(res -> LOGGER.info("Moved rc={} from {} to {} element {}...", res, source, destination, member.substring(0, 50)));
    }

    @Override
    public Mono<Integer> markAllProductPagesAsIndexed() {
        return Mono.just(0); // Placeholder implementation
    }

    @Override
    public Mono<Long> addProductPageEntry(ProductEntity productEntity) {
        String key = "ProductEntity";
        String member = Encoder.encode(productEntity);
        return connection.reactive().sadd(key, member);
    }

    @Override
    public Mono<Long> addWebPageEntry(WebPageEntity webPageEntity) {
        String key = "WebPageEntity" + "." + webPageEntity.getType();
        String member = Encoder.encode(webPageEntity);
        LOGGER.trace("adding key {} val {}", key, webPageEntity.getUrl());
        return connection.reactive().sadd(key, member);
    }

    @Override
    public Flux<ProductEntity> getProducts() {
        return connection.reactive()
                .smembers("ProductEntity")
                .map(ProductEntityEncoder::decode)
                .filter(Objects::nonNull);
    }

    @Override
    public Mono<Long> getUnparsedCount(String type) {
        return connection.reactive().scard("WebPageEntity." + type);
    }

    @Override
    public Flux<WebPageEntity> getUnparsedByType(String type, Long count) {
        LOGGER.info("getUnparsedByType {} {}", type, count);
        return connection.reactive()
                .spop("WebPageEntity." + type, Math.min(count, BATCH_SIZE))
                .map(WebPageEntityEncoder::decode)
                .doOnNext(val -> LOGGER.info("SPOP'ed {} {} {}", val.getType(), val.getUrl(), val.getCategory()))
                .filter(Objects::nonNull);
    }

    @Override
    public Mono<String> cleanUp(String[] tables) {
        return connection.reactive().flushall();
    }
}
