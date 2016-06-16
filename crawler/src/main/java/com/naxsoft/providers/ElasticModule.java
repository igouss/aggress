package com.naxsoft.providers;

import com.naxsoft.database.Elastic;
import dagger.Module;
import dagger.Provides;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.net.UnknownHostException;

/**
 * Copyright NAXSoft 2015
 */
@Module
public class ElasticModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticModule.class);

    @Provides
    @Singleton
    @NotNull
    public static Elastic getElastic() {

        Elastic elastic = new Elastic();
        try {
            elastic.connect("localhost", 9300);
            return elastic;
        } catch (UnknownHostException e) {
            LOGGER.error("Failed to connect to elastic search", e);
        }
        return null;
    }
}
