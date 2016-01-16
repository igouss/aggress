package com.naxsoft.handlers;

import com.naxsoft.utils.ElasticEscape;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;

/**
 * Copyright NAXSoft 2015
 */
public abstract class AbstractSearchHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSearchHandler.class);
    protected TransportClient client;


    public AbstractSearchHandler(TransportClient client) {
        this.client = client;
    }

    @Override
    public abstract void handleRequest(HttpServerExchange exchange) throws Exception;

}
