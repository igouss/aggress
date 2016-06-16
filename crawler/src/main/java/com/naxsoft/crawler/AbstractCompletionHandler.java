package com.naxsoft.crawler;

import org.asynchttpclient.AsyncCompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Base class to handle completed page download.
 * Logs on errors.
 */
public abstract class AbstractCompletionHandler<R> extends AsyncCompletionHandler<R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCompletionHandler.class);

    @Override
    public void onThrowable(Throwable t) {
        if (t instanceof java.util.concurrent.CancellationException) {
            // ignore
            LOGGER.debug("HTTP Request canceled");
        } else {
            LOGGER.error("HTTP Error", t);
        }
    }
}
