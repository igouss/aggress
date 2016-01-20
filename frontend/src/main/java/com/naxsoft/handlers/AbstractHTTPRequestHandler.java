package com.naxsoft.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractHTTPRequestHandler implements HttpHandler {
    /**
     * Tell the browser to disable resource caching
     * @param exchange An HTTP server request/response exchange.
     */
    protected static void disableCache(HttpServerExchange exchange)  {
        exchange.getResponseHeaders().add(Headers.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        exchange.getResponseHeaders().add(Headers.PRAGMA, "no-cache");
        exchange.getResponseHeaders().add(Headers.EXPIRES, "0");
    }
}
