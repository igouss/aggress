package com.naxsoft.providers;

import com.naxsoft.database.Database;
import dagger.Module;
import dagger.Provides;

import javax.annotation.Nonnull;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright NAXSoft 2015
 */
@Module
public class DatabaseModule {
    @Provides
    @Singleton
    @NotNull
    public Database get() {
        return new Database();
    }
}
