package com.naxsoft.handlers;

import com.naxsoft.utils.ElasticEscape;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;

/**
 * Copyright NAXSoft 2015
 */
public class SearchHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchHandler.class);
    private static final String[] includeFields = new String[]{
            "url",
            "productImage",
            "regularPrice",
            "specialPrice",
            "productName",
            "category",
    };
    private final TransportClient client;

    /**
     * @param client
     */
    public SearchHandler(TransportClient client) {
        this.client = client;
    }

    /**
     * @param searchResponse
     * @return
     */
    private static String searchResultToJson(SearchResponse searchResponse) {
        LOGGER.debug(searchResponse.toString());
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        StringBuilder builder = new StringBuilder();
        int length = searchHits.length;
        builder.append("[");
        for (int i = 0; i < length; i++) {
            if (0 == i) {
                builder.append(searchHits[i].getSourceAsString());
            } else {
                builder.append(",");
                builder.append(searchHits[i].getSourceAsString());

            }
            LOGGER.info("Score = {}", searchHits[i].getScore());
        }

        builder.append("]");
        return builder.toString();
    }

    private static int getStartFrom(RoutingContext routingContext) {
        String startFrom = routingContext.request().getParam("startFrom");
        if (startFrom != null && !startFrom.isEmpty()) {
            return Integer.parseInt(startFrom);
        }
        return 0;
    }


    private static String getSearchKey(RoutingContext routingContext, String parameter) {
        StringWriter sw = new StringWriter();
        String param = routingContext.request().getParam(parameter);
        try {
            ElasticEscape.escape(param, sw);
        } catch (Exception e) {
            LOGGER.error("Failed to escape param {}", param);
        }
        return sw.toString();
    }

    /**
     * @param searchKey
     * @param category
     * @param startFrom
     * @return
     */
    private ActionFuture<SearchResponse> runSearch(String searchKey, String category, int startFrom) {
        String indexSuffix = "";//"""-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        MultiMatchQueryBuilder searchQuery = QueryBuilders.multiMatchQuery(searchKey, "productName^4", "description^2", "_all");
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        boolQueryBuilder.must(searchQuery);
        if (null != category && !category.isEmpty() && !category.equalsIgnoreCase("all")) {
            boolQueryBuilder.must(QueryBuilders.existsQuery("category"));
            boolQueryBuilder.filter(QueryBuilders.termQuery("category", category));
        }

        LOGGER.info("{}", boolQueryBuilder);

        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("product" + indexSuffix);
        searchRequestBuilder.setQuery(boolQueryBuilder);
        searchRequestBuilder.setTypes("guns");
        searchRequestBuilder.setSearchType(SearchType.DEFAULT);
        searchRequestBuilder.setFetchSource(includeFields, null);
        searchRequestBuilder.setFrom(startFrom).setSize(30).setExplain(true);

        return searchRequestBuilder.execute();
    }

    public void handleRequestVertX(RoutingContext routingContext) {
        String searchKey = getSearchKey(routingContext, "searchKey");
        String categoryKey = getSearchKey(routingContext, "categoryKey");
        int startFrom = getStartFrom(routingContext);
        LOGGER.info("searchKey={} category={} startfrom={}", searchKey, categoryKey, startFrom);

        ActionFuture<SearchResponse> future = runSearch(searchKey, categoryKey, startFrom);
        SearchResponse searchResponse = future.actionGet();
        String result = searchResultToJson(searchResponse);

        HttpServerResponse response = routingContext.response();
        response.setChunked(true);
        response.putHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.putHeader("Pragma", "no-cache");
        response.putHeader("Expires", "0");
        response.putHeader("content-type", "application/json;charset=UTF-8");
        response.putHeader("Access-Control-Allow-Origin", "*");
        response.end(result);
    }
}
