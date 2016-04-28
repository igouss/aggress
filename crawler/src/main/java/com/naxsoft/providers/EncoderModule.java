package com.naxsoft.providers;

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
    public static WebPageEntityEncoder getWebPageEntityEncoder() {
        return new WebPageEntityEncoder();
    }

    @Provides
    @Singleton
    @NotNull
    public static ProductEntityEncoder getProductEntityEncoder() {
        return new ProductEntityEncoder();
    }
}
