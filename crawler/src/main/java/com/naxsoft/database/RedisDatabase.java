package com.naxsoft.database;


import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnectionPool;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import com.lambdaworks.redis.event.metrics.CommandLatencyEvent;
import com.lambdaworks.redis.metrics.DefaultCommandLatencyCollectorOptions;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.lambdaworks.redis.resource.ClientResources;
import com.lambdaworks.redis.resource.DefaultClientResources;
import com.naxsoft.encoders.ProductEntityEncoder;
import com.naxsoft.encoders.WebPageEntityEncoder;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
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
    ClientResources res;

    @Inject
    ProductEntityEncoder productEntityEncoder;
    @Inject
    WebPageEntityEncoder webPageEntityEncoder;
    private RedisClient redisClient;
    private StatefulRedisPubSubConnection<String, String> pubSub;
    private RedisConnectionPool<RedisAsyncCommands<String, String>> pool;
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
                .subscribe(e -> LOGGER.info(e.getLatencies().toString()));
        pubSub = redisClient.connectPubSub();
        pool = redisClient.asyncPool();
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
    public Observable<Integer> markWebPageAsParsed(WebPageEntity webPageEntity) {
        if (webPageEntity == null) {
            return Observable.error(new Exception("Trying to mark null WebPageEntity as parsed"));
        }

        String source = "WebPageEntity." + webPageEntity.getType();
        String destination = "WebPageEntity." + webPageEntity.getType() + ".parsed";
        String member = webPageEntityEncoder.encode(webPageEntity);
        return connection.reactive()
                .smove(source, destination, member)
                .map(value -> value ? 1 : 0);
    }

    @Override
    public Observable<Integer> markAllProductPagesAsIndexed() {
        return null;
    }

    @Override
    public Observable<Long> save(ProductEntity productEntity) {
        String key = "ProductEntity";
        String value = productEntityEncoder.encode(productEntity);

        return connection.reactive()
                .sadd(key, value);
    }

    @Override
    public Observable<Long> save(WebPageEntity webPageEntity) {
        String key = getKey(webPageEntity);
        String member = webPageEntityEncoder.encode(webPageEntity);
        return connection.reactive()
                .sadd(key, member);
    }

    private String getKey(WebPageEntity webPageEntity) {
        return "WebPageEntity." + webPageEntity.getType();
    }

    @Override
    public Observable<ProductEntity> getProducts() {
        return connection.reactive().smembers("ProductEntity").map(productEntityEncoder::decode);
    }

    @Override
    public Observable<WebPageEntity> getUnparsedByType(String type, Long count) {
        return connection.reactive().srandmember("WebPageEntity." + type, Math.min(BATCH_SIZE, count)).map(webPageEntityEncoder::decode);
    }

    @Override
    public Observable<Long> cleanUp(String[] tables) {
        Observable<Long> result = Observable.empty();
        for (String table : tables) {
            if (table.equalsIgnoreCase("WebPageEntity")) {
                result = Observable.concat(result, connection.reactive().del(
                        "WebPageEntity.frontPage",
                        "WebPageEntity.productList",
                        "WebPageEntity.productPage.parsed",
                        "WebPageEntity.frontPage.parsed",
                        "WebPageEntity.productList.parsed",
                        "WebPageEntity.productPage.parsed"
                ));
            } else if (table.equalsIgnoreCase("ProductEntity")) {
                result = Observable.concat(result, connection.reactive().del("ProductEntity"));
            }
        }
        return result;
    }
}
