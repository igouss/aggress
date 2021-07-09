package com.naxsoft.storage.redis;


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
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class RedisDatabase implements Persistent {
    private static final int BATCH_SIZE = 20;

    private final ClientResources res;
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;

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
        //.doOnNext(res -> log.info("Moved rc={} from {} to {} element {}...", res, source, destination, member.substring(0, 50)));
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
        log.trace("adding key {} val {}", key, webPageEntity.getUrl());
        return connection.sync().sadd(key, member);
    }

    @Override
    public List<ProductEntity> getProducts() {
        Set<String> productEntity = connection.sync()
                .smembers("ProductEntity");
        List<ProductEntity> result = new ArrayList<>();
        for (String s : productEntity) {
            result.add(ProductEntityEncoder.decode(s));
        }
        return result;
    }

    @Override
    public Long getUnparsedCount(String type) {
        return connection.sync().scard("WebPageEntity." + type);
    }

    @Override
    public List<WebPageEntity> getUnparsedByType(String type) {
        log.info("getUnparsedByType {}", type);
        List<WebPageEntity> result = new ArrayList<>();
        Set<String> spop = connection.sync().spop("WebPageEntity." + type, BATCH_SIZE);
        for (String page : spop) {
            result.add(WebPageEntityEncoder.decode(page));
        }
        return result;
    }

    @Override
    public String cleanUp(String[] tables) {
        return connection.sync().flushall();
    }
}
