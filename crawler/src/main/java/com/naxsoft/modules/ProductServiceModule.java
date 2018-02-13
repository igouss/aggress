package com.naxsoft.modules;

import com.naxsoft.parsingService.ProductService;
import com.naxsoft.storage.Persistent;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

@Module(includes = {RedisModule.class})
public class ProductServiceModule {
    @Provides
    @Singleton
    @NotNull
    static ProductService provideProductService(Persistent db) {
        return new ProductService(db);
    }
}
