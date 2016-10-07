package com.naxsoft.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Copyright NAXSoft 2015
 */
public class HealthMonitor implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger("HealthMonitor");

    public static void start() {
        Thread healthCheck = new Thread(new HealthMonitor(), "HealthMonitor");
        healthCheck.setDaemon(true);
        healthCheck.setUncaughtExceptionHandler((t, e) -> LOGGER.error("DEAD"));
        healthCheck.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(TimeUnit.MINUTES.toMillis(1));
            } catch (InterruptedException e) {
                throw new HealthCheckException("HealthMonitor Interrupted", e);
            }
            LOGGER.info("ALIVE");
        }
    }

    private class HealthCheckException extends RuntimeException {
        HealthCheckException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
