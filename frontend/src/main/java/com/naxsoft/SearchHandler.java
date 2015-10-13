package com.naxsoft;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

/**
 * Copyright NAXSoft 2015
 */
public class SearchHandler implements HttpHandler {
    private TransportClient client;
    public SearchHandler(TransportClient client) {
        this.client = client;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String searchKey = exchange.getQueryParameters().get("searchKey").getFirst();

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

        SearchResponse searchResponse = searchRequestBuilder.execute().actionGet();
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
