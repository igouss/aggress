package com.naxsoft.modules;

import com.naxsoft.database.Persistent;
import com.naxsoft.database.RedisDatabase;
import com.naxsoft.utils.PropertyNotFoundException;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright NAXSoft 2015
 */
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
