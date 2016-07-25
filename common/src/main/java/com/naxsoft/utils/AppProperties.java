package com.naxsoft.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Copyright NAXSoft 2015
 * User configurable application configuration state
 */
public class AppProperties {
    private static final Logger LOGGER = LoggerFactory.getLogger(AppProperties.class);
    private static final Properties PROPERTIES = new Properties();

    /*
     * Read common properties file on start-up
     */
    static {
        String configFileName = "config.properties";
        URL configLocation = AppProperties.class.getClassLoader().getResource(configFileName);
        try {
            LOGGER.debug("Loading config.properties");
            InputStream resourceAsStream = AppProperties.class.getClassLoader().getResourceAsStream(configFileName);
            if (resourceAsStream.available() <= 0) {
                LOGGER.error("Config file is empty {}", configLocation);
            } else {
                PROPERTIES.load(resourceAsStream);
                /* TODO: mask password */
                LOGGER.debug("App properties {}", PROPERTIES);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load properties {}", configLocation, e);
        }

        /*
         * Load config-{DEPLOYMENT_ENV}.properties it might over-write common properties
         */
        String deployment_env = System.getenv("DEPLOYMENT_ENV");
        if (deployment_env != null && !deployment_env.isEmpty()) {
            String deploymentConfigFile = "config-" + deployment_env + ".properties";
            URL deploymentConfigFileLocation = AppProperties.class.getClassLoader().getResource(deploymentConfigFile);
            try {
                LOGGER.debug("Loading " + deploymentConfigFile);
                InputStream resourceAsStream = AppProperties.class.getClassLoader().getResourceAsStream(deploymentConfigFile);
                if (resourceAsStream.available() <= 0) {
                    PROPERTIES.load(resourceAsStream);
                    LOGGER.debug("App properties {}", PROPERTIES);
                } else {
                    LOGGER.debug("config is missing or empty does not exist {}", deploymentConfigFileLocation);
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load properties: " + deploymentConfigFile, e);
            }
        }
    }

    /**
     * Return application property
     *
     * @param key Lookup key
     * @return Key value
     */
    public static Property<String> getProperty(String key) throws PropertyNotFoundException {
        String property = PROPERTIES.getProperty(key);
        if (null == property) {
            throw new PropertyNotFoundException("Unable to find property: " + key);
        } else if (property.startsWith("$")) {
            property = System.getenv(key.substring(1));
        }
        return new Property<>(property);
    }
}
