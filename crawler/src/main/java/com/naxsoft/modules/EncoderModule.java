package com.naxsoft.modules;

import com.naxsoft.encoders.ProductEntityEncoder;
import com.naxsoft.encoders.WebPageEntityEncoder;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;


@Module
public class EncoderModule {
    @Provides
    @Singleton
    @NotNull
    static WebPageEntityEncoder provideWebPageEntityEncoder() {
        return new WebPageEntityEncoder();
    }

    @Provides
    @Singleton
    @NotNull
    static ProductEntityEncoder provideProductEntityEncoder() {
        return new ProductEntityEncoder();
    }
}
