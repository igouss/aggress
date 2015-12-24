package com.naxsoft.handlers;

import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class ErrorHandler implements HttpHandler {

    private HttpHandler next;

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        try {
            next.handleRequest(exchange);
        } catch (Exception e) {
            if (exchange.isResponseChannelAvailable()) {
                exchange.getResponseSender().send(e.toString() + "\n" + e.getCause().toString());
            }
        }
    }

    public ErrorHandler setNext(final HttpHandler next) {
        Handlers.handlerNotNull(next);
        this.next = next;
        return this;
    }

}
