package com.naxsoft.modules;

import com.naxsoft.parsingService.WebPageService;
import com.naxsoft.storage.Persistent;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

@Module(includes = {RedisModule.class})
public class WebPageServiceModule {
    @Provides
    @Singleton
    @NotNull
    static WebPageService provideWebPageService(Persistent db) {
        return new WebPageService(db);
    }
}
