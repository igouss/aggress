package com.naxsoft.database;


import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnectionPool;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.naxsoft.encoders.ProductEntityEncoder;
import com.naxsoft.encoders.WebPageEntityEncoder;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.utils.AppProperties;
import org.hibernate.StatelessSession;
import rx.Observable;
import rx.functions.Func1;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class RedisDatabase implements Persistent {
    @Inject
    ProductEntityEncoder productEntityEncoder;
    @Inject
    WebPageEntityEncoder webPageEntityEncoder;
    private RedisClient redisClient;
    private StatefulRedisPubSubConnection<String, String> pubSub;
    private RedisConnectionPool<RedisAsyncCommands<String, String>> pool;
    private StatefulRedisConnection<String, String> connection;

    public RedisDatabase() {
        this(AppProperties.getProperty("redisHost"), Integer.parseInt(AppProperties.getProperty("redisPort")));
    }

    private RedisDatabase(String host, int port) {
        redisClient = RedisClient.create(RedisURI.Builder.redis(host, port).build());
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
        connection.close();
        redisClient.shutdown();
    }

    @Override
    public Observable<Long> getUnparsedCount(String type) {
        return connection.reactive().scard("WebPageEntity." + type);
    }

    @Override
    public Observable<Integer> markWebPageAsParsed(WebPageEntity entity) {
        return connection.reactive()
                .smove("WebPageEntity" + entity.getType(), "WebPageEntity.parsed" + entity.getType(), getKey(entity))
                .map(value -> value ? 1 : 0);
    }

    @Override
    public Observable<Integer> markAllProductPagesAsIndexed() {
        return null;
    }

    @Override
    public Observable<Boolean> save(ProductEntity productEntity) {
        String key = "ProductEntity";
        String value = productEntityEncoder.encode(productEntity);
        return connection.reactive()
                .sadd(key, value).map(rc -> rc == 1);
    }

    @Override
    public Observable<Boolean> save(WebPageEntity webPageEntity) {
        String key = getKey(webPageEntity);
        String value = webPageEntityEncoder.encode(webPageEntity);
        return connection.reactive()
                .sadd(key, value).map(rc -> rc == 1);
    }

    private String getKey(WebPageEntity webPageEntity) {
        return "WebPageEntity." + webPageEntity.getType();
    }

    @Override
    public Observable<ProductEntity> getProducts() {
        return connection.reactive().smembers("ProductEntity").map(productEntityEncoder::decode);
    }

    @Override
    public Observable<WebPageEntity> getUnparsedByType(String type) {
        return connection.reactive().smembers("WebPageEntity." + type).map(webPageEntityEncoder::decode);
    }

    @Override
    public <R> Observable<R> executeTransaction(Func1<StatelessSession, R> action) {
        return null;
    }

    @Override
    public <T> Observable<T> scroll(String queryString) {
        return null;
    }


}
