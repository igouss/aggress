package com.naxsoft.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Copyright NAXSoft 2015
 */
public class AppProperties {
    private static final Logger logger = LoggerFactory.getLogger(AppProperties.class);
    private static final Properties properties = new Properties();

    static {
        try {
            InputStream resourceAsStream = AppProperties.class.getClassLoader().getResourceAsStream("config.properties");
            properties.load(resourceAsStream);
            logger.debug("App properties {}" , properties);
        } catch (Exception e) {
            logger.error("Failed to load properties", e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
