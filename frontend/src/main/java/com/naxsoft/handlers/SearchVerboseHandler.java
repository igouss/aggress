package com.naxsoft.handlers;

import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copyright NAXSoft 2015
 */
public class SearchVerboseHandler extends AbstractSearchHandler {
    private static final Logger logger = LoggerFactory.getLogger(SearchHandler.class);

    public SearchVerboseHandler(TransportClient client) {
        super(client);
    }

    @Override
    protected ListenableActionFuture<SearchResponse> runSearch(String searchKey, int startFrom) {
        String indexSuffix = "";//"""-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());
//        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery(searchKey);
        MultiMatchQueryBuilder queryBuilder = new MultiMatchQueryBuilder(searchKey, "productName^2");
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("product" + indexSuffix);
        searchRequestBuilder.setTypes("guns");
        searchRequestBuilder.setSearchType(SearchType.DEFAULT);
        searchRequestBuilder.setQuery(queryBuilder);
        searchRequestBuilder.setFrom(startFrom).setSize(10).setExplain(true);

        return searchRequestBuilder.execute();
    }

}

