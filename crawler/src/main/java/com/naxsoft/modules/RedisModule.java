package com.naxsoft.modules;

import com.naxsoft.storage.Persistent;
import com.naxsoft.storage.redis.RedisDatabase;
import com.naxsoft.utils.PropertyNotFoundException;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;


@Module()
public class RedisModule {
    @Provides
    @Singleton
    @NotNull
    static Persistent provideRedisDatabase() {
        try {
            return new RedisDatabase();
        } catch (PropertyNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
