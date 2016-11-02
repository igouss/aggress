package com.naxsoft.modules;

import com.naxsoft.parsingService.ShellService;
import dagger.Module;
import dagger.Provides;
import io.vertx.core.Vertx;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright Ciena 02-11-16.
 */
@Module()
public class ShellServiceModule {
    @Provides
    @Singleton
    @NotNull
    static ShellService ShellModule(Vertx vertx) {
        return new ShellService(vertx);
    }
}
