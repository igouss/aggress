package com.naxsoft.database;


import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisConnectionPool;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import com.lambdaworks.redis.api.async.RedisAsyncCommands;
import com.lambdaworks.redis.pubsub.StatefulRedisPubSubConnection;
import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import com.naxsoft.utils.AppProperties;
import org.hibernate.StatelessSession;
import rx.Observable;
import rx.functions.Func1;

import javax.inject.Singleton;

@Singleton
public class RedisDatabase implements Persistent {
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

    @Override
    public void close() {
        connection.close();
        redisClient.shutdown();
    }

    @Override
    public Observable<Long> getUnparsedCount() {
        return null;
    }

    @Override
    public Observable<Long> getUnparsedCount(String type) {
        return null;
    }

    @Override
    public Observable<Integer> markWebPageAsParsed(Long webPageEntryId) {
        return null;
    }

    @Override
    public Observable<Integer> markAllProductPagesAsIndexed() {
        return null;
    }

    @Override
    public Observable<Boolean> save(ProductEntity productEntity) {
        String key = productEntity.getUrl();
        String value = "";
        return connection.reactive().set(key, value).map(result -> result.equalsIgnoreCase("OK"));
    }

    @Override
    public Observable<Boolean> save(WebPageEntity webPageEntity) {
        return null;
    }

    @Override
    public Observable<ProductEntity> getProducts() {
        return null;
    }

    @Override
    public Observable<WebPageEntity> getUnparsedByType(String type) {
        return null;
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
