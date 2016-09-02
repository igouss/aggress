package com.naxsoft.modules;

import com.naxsoft.scheduler.Scheduler;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright NAXSoft 2015
 */
@Module
public class SchedulerModule {
    @Provides
    @Singleton
    @NotNull
    public static Scheduler get() {
        return new Scheduler();
    }
}
