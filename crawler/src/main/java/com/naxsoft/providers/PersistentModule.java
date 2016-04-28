package com.naxsoft.providers;

import com.naxsoft.database.Persistent;
import com.naxsoft.database.RedisDatabase;
import com.naxsoft.encoders.ProductEntityEncoder;
import com.naxsoft.encoders.WebPageEntityEncoder;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright NAXSoft 2015
 */
@Module(includes = {EncoderModule.class})
public class PersistentModule {
    @Provides
    @Singleton
    @NotNull
    public static Persistent providePersistent(WebPageEntityEncoder webPageEntityEncoder, ProductEntityEncoder productEntityEncoder) {
        RedisDatabase redisDatabase = new RedisDatabase();
        redisDatabase.setWebPageEntityEncoder(webPageEntityEncoder);
        redisDatabase.setProductEntityEncoder(productEntityEncoder);
        return redisDatabase;
    }
}
