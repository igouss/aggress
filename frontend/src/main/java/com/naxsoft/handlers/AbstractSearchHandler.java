package com.naxsoft.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.elasticsearch.client.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 *
 *
 */
public abstract class AbstractSearchHandler implements HttpHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSearchHandler.class);
    protected TransportClient client;

    /**
     *
     * @param client
     */
    public AbstractSearchHandler(TransportClient client) {
        this.client = client;
    }

    /**
     *
     * @param exchange
     * @throws Exception
     */
    @Override
    public abstract void handleRequest(HttpServerExchange exchange) throws Exception;

}
