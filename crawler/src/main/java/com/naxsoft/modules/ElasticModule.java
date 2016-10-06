package com.naxsoft.modules;

import com.naxsoft.storage.elasticsearch.Elastic;
import com.naxsoft.utils.AppProperties;
import com.naxsoft.utils.PropertyNotFoundException;
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
    static Elastic provideElastic() {
        Elastic elastic = new Elastic();
        try {
            String elasticHost = AppProperties.getProperty("elasticHost").getValue();
            int elasticPort = Integer.parseInt(AppProperties.getProperty("elasticPort").getValue());
            try {
                elastic.connect(elasticHost, elasticPort);
                return elastic;
            } catch (UnknownHostException e) {
                LOGGER.error("Failed to connect to elastic search " + elasticHost + ":" + elasticPort, e);
            }
        } catch (PropertyNotFoundException e) {
            LOGGER.error("Failed to load elasticProperty: " + e.getMessage(), e);
        }
        return null;
    }
}
