package com.naxsoft.modules;

import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright Naxsoft 2016.
 */
@Module
public class VertxModule {
    @Provides
    @Singleton
    @NotNull
    static Vertx provideProductService() {
        // use the JVM built-in resolver
        System.setProperty("vertx.disableDnsResolver", "true");
        return Vertx.vertx();
    }
}
