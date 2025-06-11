package com.naxsoft.modules;

import com.codahale.metrics.MetricRegistry;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;


@Module
public class MetricsRegistryModule {
    @Provides
    @Singleton
    @NotNull
    static MetricRegistry provideMetricRegistry() {
        return new MetricRegistry();
    }
}
