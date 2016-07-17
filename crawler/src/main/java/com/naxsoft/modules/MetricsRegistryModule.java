package com.naxsoft.modules;

import com.codahale.metrics.MetricRegistry;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;

/**
 * Copyright NAXSoft 2015
 */
@Module
public class MetricsRegistryModule {
    @Provides
    @Singleton
    @NotNull
    static MetricRegistry get() {
        return new MetricRegistry();
    }
}
