package com.naxsoft.modules;

import com.naxsoft.database.Persistent;
import com.naxsoft.database.ProductService;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright NAXSoft 2015
 */
@Module(includes = {RedisModule.class})
public class ProductServiceModule {
    @Provides
    @Singleton
    @NotNull
    static ProductService provideProductService(Persistent db) {
        return new ProductService(db);
    }
}
