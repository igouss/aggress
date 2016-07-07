package com.naxsoft.crawler;

import org.asynchttpclient.AsyncCompletionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 * <p>
 * Base class to handle completed page download.
 * Logs on errors.
 */
public abstract class AbstractCompletionHandler<R> extends AsyncCompletionHandler<R> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCompletionHandler.class);
    private ProxyManager proxyManager;

    @Override
    public void onThrowable(Throwable t) {
        if (t instanceof java.util.concurrent.CancellationException) {
            // ignore
            LOGGER.debug("HTTP Request canceled");
        } else if (t instanceof java.net.ConnectException) {
            handleProxyFailure(t);
        } else {
            LOGGER.error("HTTP Error", t);
        }
    }

    private void handleProxyFailure(Throwable t) {
        String message = t.getMessage(); // Connection refused: no further information: /127.0.0.1:8080
        LOGGER.error(message);
        Pattern p = Pattern.compile("/(.+):");
        Matcher m = p.matcher(message);
        if (m.find()) {
            String address = m.group(1);
            LOGGER.error("Proxy {} failed", address);
            if (proxyManager != null) {
                proxyManager.proxyFailed(address);
            }
        }
    }

    void setProxyManager(ProxyManager proxyManager) {
        this.proxyManager = proxyManager;
    }
}
