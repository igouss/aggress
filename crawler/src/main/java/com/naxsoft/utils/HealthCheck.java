package com.naxsoft.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Copyright NAXSoft 2015
 */
public class HealthCheck implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger("HealthCheck");

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(1));
            } catch (InterruptedException e) {
                throw new HealthCheckException("HealthCheck Interrupted", e);
            }
            LOGGER.info("ALIVE");
        }
    }

    public static void start() {
        Thread healthCheck = new Thread(new HealthCheck(), "HealthCheck");
        healthCheck.setDaemon(true);
        healthCheck.setUncaughtExceptionHandler((t, e) -> {
            LOGGER.error("DEAD");
        });
        healthCheck.run(); // force log
        healthCheck.start();
    }

    private class HealthCheckException extends RuntimeException {
        HealthCheckException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
