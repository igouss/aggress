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

    private static final String[] includeFields = new String[]{
            "url",
            "productImage",
            "regularPrice",
            "specialPrice",
            "productName",
    };

    public AbstractSearchHandler(TransportClient client) {
        this.client = client;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String searchKey = getSearchKey(exchange);
        String categoryKey = getCategoryKey(exchange);
        int startFrom = getStartFrom(exchange);

        ListenableActionFuture<SearchResponse> future = runSearch(searchKey, categoryKey, startFrom);
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

    private String getCategoryKey(HttpServerExchange exchange) throws Exception {
        StringWriter sw = new StringWriter();
        String val = exchange.getQueryParameters().get("categoryKey").getFirst();
        ElasticEscape.escape(val, sw);
        return sw.toString();
    }

    protected ListenableActionFuture<SearchResponse> runSearch(String searchKey, String category, int startFrom) {
        String indexSuffix = "";//"""-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.should(QueryBuilders.multiMatchQuery(searchKey, "productName^3", "description", "category"));
        boolQueryBuilder.filter(QueryBuilders.existsQuery("category"));
        boolQueryBuilder.must(QueryBuilders.matchQuery("category", category).type(MatchQueryBuilder.Type.PHRASE));

        logger.info("{}", boolQueryBuilder);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("product" + indexSuffix);
        searchRequestBuilder.setQuery(boolQueryBuilder);
        searchRequestBuilder.setTypes("guns");
        searchRequestBuilder.setSearchType(SearchType.DEFAULT);
        searchRequestBuilder.setFetchSource(includeFields, null);
        searchRequestBuilder.setFrom(startFrom).setSize(30).setExplain(true);

        return searchRequestBuilder.execute();
    }
}
