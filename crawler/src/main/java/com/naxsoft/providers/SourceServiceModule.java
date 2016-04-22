package com.naxsoft.providers;

import com.naxsoft.database.Persistent;
import com.naxsoft.database.SourceService;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright NAXSoft 2015
 */
@Module(includes = {PersistentModule.class})
public class SourceServiceModule {
    @Provides
    @Singleton
    @NotNull
    public static SourceService get(Persistent db) {
        return new SourceService(db);
    }
}
