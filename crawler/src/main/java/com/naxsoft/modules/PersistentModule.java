package com.naxsoft.modules;

import com.naxsoft.database.Persistent;
import com.naxsoft.database.RedisDatabase;
import com.naxsoft.encoders.ProductEntityEncoder;
import com.naxsoft.encoders.WebPageEntityEncoder;
import com.naxsoft.utils.PropertyNotFoundException;
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
    static Persistent providePersistent(WebPageEntityEncoder webPageEntityEncoder, ProductEntityEncoder productEntityEncoder) {
        RedisDatabase redisDatabase = null;
        try {
            redisDatabase = new RedisDatabase();
            redisDatabase.setWebPageEntityEncoder(webPageEntityEncoder);
            redisDatabase.setProductEntityEncoder(productEntityEncoder);
            return redisDatabase;
        } catch (PropertyNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
