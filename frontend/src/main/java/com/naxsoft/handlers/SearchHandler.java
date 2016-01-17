package com.naxsoft.handlers;

import com.naxsoft.utils.ElasticEscape;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;

/**
 * Copyright NAXSoft 2015
 */
public class SearchHandler extends AbstractSearchHandler {
    private static final Logger logger = LoggerFactory.getLogger(SearchHandler.class);
    private static final String[] includeFields = new String[]{
            "url",
            "productImage",
            "regularPrice",
            "specialPrice",
            "productName",
    };

    /**
     * @param client
     */
    public SearchHandler(TransportClient client) {
        super(client);
    }

    /**
     * @param searchResponse
     * @return
     */
    private static String searchResultToJson(SearchResponse searchResponse) {
        logger.debug(searchResponse.toString());
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        StringBuilder builder = new StringBuilder();
        int length = searchHits.length;
        builder.append("[");
        for (int i = 0; i < length; i++) {
            if (searchHits[i].getScore() < 0.4) {
                continue;
            }
            if (i == 0) {
                builder.append(searchHits[i].getSourceAsString());
            } else {
                builder.append(",");
                builder.append(searchHits[i].getSourceAsString());

            }
            logger.info("Score = {}", searchHits[i].getScore());
        }

        builder.append("]");
        return builder.toString();
    }

    /**
     * @param exchange
     * @return
     */
    private static int getStartFrom(HttpServerExchange exchange) {
        int startFrom = 0;
        if (exchange.getQueryParameters().containsKey("startFrom")) {
            startFrom = Integer.parseInt(exchange.getQueryParameters().get("startFrom").getFirst());
        }
        return startFrom;
    }

    /**
     * @param exchange
     * @param paremeter
     * @return
     * @throws Exception
     */
    private static String getSearchKey(HttpServerExchange exchange, String paremeter) throws Exception {
        StringWriter sw = new StringWriter();
        String val = exchange.getQueryParameters().get(paremeter).getFirst();
        ElasticEscape.escape(val, sw);
        return sw.toString();
    }

    /**
     * @param exchange
     * @throws Exception
     */
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String searchKey = getSearchKey(exchange, "searchKey");
        String categoryKey = getSearchKey(exchange, "categoryKey");
        int startFrom = getStartFrom(exchange);
        logger.info("searchKey={} category={} startfrom={}", searchKey, categoryKey, startFrom);

        ListenableActionFuture<SearchResponse> future = runSearch(searchKey, categoryKey, startFrom);
        SearchResponse searchResponse = future.actionGet();
        String result = searchResultToJson(searchResponse);

        exchange.getResponseHeaders().add(HttpString.tryFromString("Access-Control-Allow-Origin"), "*");
        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/json;");
        exchange.getResponseSender().send(result);
    }

    /**
     * @param searchKey
     * @param category
     * @param startFrom
     * @return
     */
    protected ListenableActionFuture<SearchResponse> runSearch(String searchKey, String category, int startFrom) {
        String indexSuffix = "";//"""-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        MatchQueryBuilder categoryFilter = QueryBuilders.matchQuery("category", category).type(MatchQueryBuilder.Type.PHRASE);
        MultiMatchQueryBuilder searchQuery = QueryBuilders.multiMatchQuery(searchKey, "productName^3", "description", "category").type(MultiMatchQueryBuilder.Type.PHRASE);
        ExistsQueryBuilder hasCategory = QueryBuilders.existsQuery("category");

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        boolQueryBuilder.should(searchQuery);
//        boolQueryBuilder.filter(hasCategory);
        boolQueryBuilder.filter(categoryFilter);


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
