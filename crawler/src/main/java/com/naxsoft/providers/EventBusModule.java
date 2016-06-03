package com.naxsoft.providers;

import com.lambdaworks.redis.event.DefaultEventBus;
import com.lambdaworks.redis.event.EventBus;
import dagger.Module;
import dagger.Provides;
import rx.schedulers.Schedulers;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright NAXSoft 2015
 */
@Module
public class EventBusModule {
    @Provides
    @Singleton
    @NotNull
    public static EventBus getWebPageEntityEncoder() {
        return new DefaultEventBus(Schedulers.computation());
    }
}
