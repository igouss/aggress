package com.naxsoft.handlers;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public SearchHandler(TransportClient client) {
        super(client);
    }

    //    @Override
//    protected ListenableActionFuture<SearchResponse> runSearch(String searchKey, int startFrom) {
//        String indexSuffix = "";//"""-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
////        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery(searchKey);
//        MultiMatchQueryBuilder queryBuilder = new MultiMatchQueryBuilder(searchKey, "productName^2");
//        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("product" + indexSuffix);
//        searchRequestBuilder.setTypes("guns");
//        searchRequestBuilder.setSearchType(SearchType.DEFAULT);
//        searchRequestBuilder.setQuery(queryBuilder);
//        searchRequestBuilder.setFetchSource(includeFields, null);
//        searchRequestBuilder.setFrom(startFrom).setSize(30).setExplain(false);
//
//        return searchRequestBuilder.execute();
//    }


}
