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
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;

/**
 * Copyright NAXSoft 2015
 */
public class SearchHandler implements HttpHandler {
    private TransportClient client;
    private static final Logger logger = LoggerFactory.getLogger(SearchHandler.class);

    public SearchHandler(TransportClient client) {
        this.client = client;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        StringWriter sw = new StringWriter();
        String val = exchange.getQueryParameters().get("searchKey").getFirst();
        ElasticEscape.escape(val, sw);
        String searchKey = sw.toString();

        int startFrom = 0;
        if (exchange.getQueryParameters().containsKey("startFrom")) {
            startFrom = Integer.parseInt(exchange.getQueryParameters().get("startFrom").getFirst());
        }
        String indexSuffix = "";//"""-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery(searchKey);
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("product" + indexSuffix);
        searchRequestBuilder.setTypes("guns");
        searchRequestBuilder.setSearchType(SearchType.DEFAULT);
        searchRequestBuilder.setQuery(queryBuilder);
        searchRequestBuilder.setFrom(startFrom).setSize(10).setExplain(true);

        ListenableActionFuture<SearchResponse> future = searchRequestBuilder.execute();
        SearchResponse searchResponse = future.actionGet();
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
        String result = builder.toString();

        exchange.getResponseHeaders().add(HttpString.tryFromString("Access-Control-Allow-Origin"), "*");
        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/json;");
        exchange.getResponseSender().send(result);
    }
}
