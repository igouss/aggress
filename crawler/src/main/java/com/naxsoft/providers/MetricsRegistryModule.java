package com.naxsoft.providers;

import com.codahale.metrics.MetricRegistry;
import com.naxsoft.database.Elastic;
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
    MetricRegistry get() {
        return new MetricRegistry();
    }
}
