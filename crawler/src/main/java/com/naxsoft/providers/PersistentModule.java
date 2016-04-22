package com.naxsoft.providers;

import com.naxsoft.database.Database;
import com.naxsoft.database.Persistent;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright NAXSoft 2015
 */
@Module
public class PersistentModule {
    @Provides
    @Singleton
    @NotNull
    public static Persistent get() {
        return new Database();
    }
}
