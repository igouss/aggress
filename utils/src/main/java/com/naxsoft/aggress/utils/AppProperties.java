package com.naxsoft.aggress.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * User configurable application configuration state
 */
@Slf4j
public class AppProperties {
    private static final Properties PROPERTIES = new Properties();

    /*
     * Read common properties file on start-up
     */
    static {
        String configFileName = "config.properties";
        URL configLocation = AppProperties.class.getClassLoader().getResource(configFileName);
        try {
            log.debug("Loading config.properties");
            InputStream resourceAsStream = AppProperties.class.getClassLoader().getResourceAsStream(configFileName);
            if (resourceAsStream == null || resourceAsStream.available() <= 0) {
                log.error("Config file {} at {} is empty", configFileName, configLocation);
            } else {
                PROPERTIES.load(resourceAsStream);
                /* TODO: mask password */
                log.debug("App properties {}", PROPERTIES);
            }
        } catch (Exception e) {
            log.error("Failed to load properties {}", configLocation, e);
        }

        /*
         * Load config-{DEPLOYMENT_ENV}.properties it might over-write common properties
         */
        String deployment_env = System.getenv("DEPLOYMENT_ENV");
        if (deployment_env != null && !deployment_env.isEmpty()) {
            String deploymentConfigFile = "config-" + deployment_env + ".properties";
            URL deploymentConfigFileLocation = AppProperties.class.getClassLoader().getResource(deploymentConfigFile);
            try {
                log.debug("Loading " + deploymentConfigFile);
                InputStream resourceAsStream = AppProperties.class.getClassLoader().getResourceAsStream(deploymentConfigFile);
                if (resourceAsStream == null) {
                    throw new RuntimeException("Failed to load deploymentConfigFile");
                } else if (resourceAsStream.available() <= 0) {
                    log.debug("config is missing or empty does not exist {}", deploymentConfigFileLocation);
                } else {
                    PROPERTIES.load(resourceAsStream);
                    log.debug("App properties {}", PROPERTIES);
                }
            } catch (Exception e) {
                log.error("Failed to load properties: " + deploymentConfigFileLocation, e);
            }
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
        } else if (property.startsWith("$")) {
            property = System.getenv(key.substring(1));
        }
        return property;
    }
}
