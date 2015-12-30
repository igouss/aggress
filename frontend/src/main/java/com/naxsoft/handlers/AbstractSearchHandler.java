package com.naxsoft.handlers;

import com.naxsoft.utils.ElasticEscape;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractSearchHandler implements HttpHandler {
    protected TransportClient client;
    private static final Logger logger = LoggerFactory.getLogger(AbstractSearchHandler.class);


    public AbstractSearchHandler(TransportClient client) {
        this.client = client;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String searchKey = getSearchKey(exchange);

        int startFrom = getStartFrom(exchange);
        ListenableActionFuture<SearchResponse> future = runSearch(searchKey, startFrom);
        SearchResponse searchResponse = future.actionGet();
        String result = searchResultToJson(searchResponse);

        exchange.getResponseHeaders().add(HttpString.tryFromString("Access-Control-Allow-Origin"), "*");
        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/json;");
        exchange.getResponseSender().send(result);
    }

    private String searchResultToJson(SearchResponse searchResponse) {
        logger.debug(searchResponse.toString());
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        StringBuilder builder = new StringBuilder();
        int length = searchHits.length;
        builder.append("[");
        for (int i = 0; i < length; i++) {
            if (i == length - 1) {
                builder.append(searchHits[i].getSourceAsString());
            } else {
                builder.append(searchHits[i].getSourceAsString());
                builder.append(",");
            }
        }
        builder.append("]");
        return builder.toString();
    }
    private int getStartFrom(HttpServerExchange exchange) {
        int startFrom = 0;
        if (exchange.getQueryParameters().containsKey("startFrom")) {
            startFrom = Integer.parseInt(exchange.getQueryParameters().get("startFrom").getFirst());
        }
        return startFrom;
    }

    private String getSearchKey(HttpServerExchange exchange) throws Exception {
        StringWriter sw = new StringWriter();
        String val = exchange.getQueryParameters().get("searchKey").getFirst();
        ElasticEscape.escape(val, sw);
        return sw.toString();
    }
    protected abstract ListenableActionFuture<SearchResponse> runSearch(String searchKey, int startFrom);
}
