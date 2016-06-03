package com.naxsoft.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;

/**
 * Copyright NAXSoft 2015
 * User configurable application configuration state
 */
public class AppProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppProperties.class);
    private static final Properties PROPERTIES = new Properties();

    /**
     * Read properties file on start-up
     */
    static {
        try {
            InputStream resourceAsStream = AppProperties.class.getClassLoader().getResourceAsStream("config.properties");
            PROPERTIES.load(resourceAsStream);
            LOGGER.debug("App properties {}", PROPERTIES);
        } catch (Exception e) {
            LOGGER.error("Failed to load properties", e);
        }
    }

    /**
     * Return application property
     *
     * @param key Lookup key
     * @return Key value
     */
    public static String getProperty(String key) throws PropertyNotFoundException {
        String property = PROPERTIES.getProperty(key);
        if (null == property) {
            throw new PropertyNotFoundException("Unable to find property: " + key);
        }
        return property;
    }
}
