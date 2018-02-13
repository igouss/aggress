package com.naxsoft.modules;

import com.naxsoft.scheduler.Scheduler;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

@Module
public class SchedulerModule {
    @Provides
    @Singleton
    @NotNull
    public static Scheduler get() {
        return new Scheduler();
    }
}
