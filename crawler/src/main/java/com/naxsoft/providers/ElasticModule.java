package com.naxsoft.providers;

import com.naxsoft.database.Elastic;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright NAXSoft 2015
 */
@Module
public class ElasticModule {
    @Provides
    @Singleton
    @NotNull
    public static Elastic getElastic() {
        return new Elastic();
    }
}
