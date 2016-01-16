package com.naxsoft.crawler;

import com.ning.http.client.AsyncCompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 *
 * Base class to handle completed page download.
 * Logs on errors.
 *
 */
public abstract class CompletionHandler<R> extends AsyncCompletionHandler<R> {
    private static final Logger logger = LoggerFactory.getLogger(CompletionHandler.class);

    /**
     *
     * @param t
     */
    @Override
    public void onThrowable(Throwable t) {
        if (t instanceof java.util.concurrent.CancellationException) {
            // ignore
            logger.debug("HTTP Request canceled");
        } else {
            logger.error("HTTP Error", t);
        }
    }
}
