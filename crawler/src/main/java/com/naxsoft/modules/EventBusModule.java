package com.naxsoft.modules;

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
    static EventBus provideWebPageEntityEncoder() {
        return new DefaultEventBus(Schedulers.computation());
    }
}
