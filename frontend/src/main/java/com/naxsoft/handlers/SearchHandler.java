package com.naxsoft.handlers;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ExistsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.fasterxml.jackson.databind.JsonNode;
import com.naxsoft.utils.ElasticEscape;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;


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
    private final ElasticsearchClient client;

    /**
     * @param client Elasticsearch Java API Client
     */
    public SearchHandler(ElasticsearchClient client) {
        this.client = client;
    }

    /**
     * Convert search response to JSON string
     * @param searchResponse Elasticsearch search response
     * @return JSON string representation
     */
    private static String searchResultToJson(SearchResponse<JsonNode> searchResponse) {
        LOGGER.debug(searchResponse.toString());
        List<Hit<JsonNode>> hits = searchResponse.hits().hits();
        StringBuilder builder = new StringBuilder();
        int length = hits.size();
        builder.append("[");
        for (int i = 0; i < length; i++) {
            Hit<JsonNode> hit = hits.get(i);
            if (0 == i) {
                builder.append(hit.source().toString());
            } else {
                builder.append(",");
                builder.append(hit.source().toString());
            }
            LOGGER.info("Score = {}", hit.score());
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
     * Execute search query using Java API Client
     * @param searchKey Search term
     * @param category Category filter
     * @param startFrom Pagination offset
     * @return CompletableFuture with search response
     */
    private CompletableFuture<SearchResponse<JsonNode>> runSearch(String searchKey, String category, int startFrom) {
        String indexSuffix = "";//"""-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Build multi-match query
                MultiMatchQuery multiMatchQuery = MultiMatchQuery.of(m -> m
                        .query(searchKey)
                        .fields("productName^4", "description^2", "_all")
                );

                // Build bool query
                BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
                boolQueryBuilder.must(multiMatchQuery._toQuery());

                // Add category filter if specified
                if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("all")) {
                    ExistsQuery existsQuery = ExistsQuery.of(e -> e.field("category"));
                    TermQuery termQuery = TermQuery.of(t -> t
                            .field("category")
                            .value(category)
                    );

                    boolQueryBuilder.must(existsQuery._toQuery());
                    boolQueryBuilder.filter(termQuery._toQuery());
                }

                BoolQuery boolQuery = boolQueryBuilder.build();
                LOGGER.info("Executing query: {}", boolQuery);

                // Build search request
                SearchRequest searchRequest = SearchRequest.of(s -> s
                        .index("product" + indexSuffix)
                        .query(boolQuery._toQuery())
                        .source(source -> source
                                .filter(f -> f
                                        .includes(Arrays.asList(includeFields))
                                )
                        )
                        .from(startFrom)
                        .size(30)
                        .explain(true)
                );

                return client.search(searchRequest, JsonNode.class);
            } catch (Exception e) {
                LOGGER.error("Search failed", e);
                throw new RuntimeException(e);
            }
        });
    }

    public void handleRequestVertX(RoutingContext routingContext) {
        String searchKey = getSearchKey(routingContext, "searchKey");
        String categoryKey = getSearchKey(routingContext, "categoryKey");
        int startFrom = getStartFrom(routingContext);
        LOGGER.info("searchKey={} category={} startfrom={}", searchKey, categoryKey, startFrom);

        runSearch(searchKey, categoryKey, startFrom)
                .whenComplete((searchResponse, throwable) -> {
                    HttpServerResponse response = routingContext.response();
                    response.setChunked(true);
                    response.putHeader("Cache-Control", "no-cache, no-store, must-revalidate");
                    response.putHeader("Pragma", "no-cache");
                    response.putHeader("Expires", "0");
                    response.putHeader("content-type", "application/json;charset=UTF-8");
                    response.putHeader("Access-Control-Allow-Origin", "*");

                    if (throwable != null) {
                        LOGGER.error("Search request failed", throwable);
                        response.setStatusCode(500);
                        response.end("{\"error\":\"Search failed\"}");
                    } else {
                        String result = searchResultToJson(searchResponse);
                        response.end(result);
                    }
                });
    }
}