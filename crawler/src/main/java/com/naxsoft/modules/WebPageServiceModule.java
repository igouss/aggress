package com.naxsoft.modules;

import com.naxsoft.database.Persistent;
import com.naxsoft.database.WebPageService;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright NAXSoft 2015
 */
@Module(includes = {RedisModule.class})
public class WebPageServiceModule {
    @Provides
    @Singleton
    @NotNull
    static WebPageService provideWebPageService(Persistent db) {
        return new WebPageService(db);
    }
}
