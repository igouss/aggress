package com.naxsoft.storage.redis;

import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Singleton;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.RedisURI;
import com.lambdaworks.redis.api.StatefulRedisConnection;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        connection = redisClient.connect();
    }

    @Override
    public void close() {
        redisClient.shutdown();
        res.shutdown();
    }

    @Override
    public Long markWebPageAsParsed(WebPageEntity webPageEntity) {
        String source = "WebPageEntity." + webPageEntity.getType();
        String destination = "WebPageEntity." + webPageEntity.getType() + ".parsed";
        String member = Encoder.encode(webPageEntity);
        return connection.sync().sadd(destination, member);
        //.doOnNext(res -> LOGGER.info("Moved rc={} from {} to {} element {}...", res, source, destination, member.substring(0, 50)));
    }

    @Override
    public Integer markAllProductPagesAsIndexed() {
        return null;
    }

    @Override
    public Long addProductPageEntry(ProductEntity productEntity) {
        String key = "ProductEntity";
        String member = Encoder.encode(productEntity);
        return connection.sync().sadd(key, member);
    }

    @Override
    public Long addWebPageEntry(WebPageEntity webPageEntity) {
        String key = "WebPageEntity" + "." + webPageEntity.getType();
        String member = Encoder.encode(webPageEntity);
        LOGGER.trace("adding key {} val {}", key, webPageEntity.getUrl());
        return connection.sync().sadd(key, member);
    }

    @Override
    public Iterable<ProductEntity> getProducts() {
        return connection.sync()
                .smembers("ProductEntity")
                .stream()
                .map(ProductEntityEncoder::decode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public Long getUnparsedCount(String type) {
        return connection.sync().scard("WebPageEntity." + type);
    }

    @Override
    public Iterable<WebPageEntity> getUnparsedByType(String type, Long count) {
        LOGGER.info("getUnparsedByType {} {}", type, count);
        return connection.sync()
                .spop("WebPageEntity." + type, Math.min(count, BATCH_SIZE))
                .stream()
                .map(WebPageEntityEncoder::decode)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public String cleanUp(String[] tables) {
        return connection.sync().flushall();
    }
}
