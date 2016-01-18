package com.naxsoft;

import com.naxsoft.database.Elastic;
import com.naxsoft.dumyData.Generator;
import com.naxsoft.entity.ProductEntity;
import org.elasticsearch.action.ListenableActionFuture;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import rx.Observable;
import rx.Subscription;

import java.util.ArrayList;

/**
 * Copyright NAXSoft 2015
 */
public class TestSearch {
    private static final String[] includeFields = new String[]{
            "url",
            "productImage",
            "regularPrice",
            "specialPrice",
            "productName",
    };
    final Elastic elastic = new Elastic();
    private final String indexSuffix = "";

    @Before
    public void before() throws Throwable {
        elastic.connect("localhost", 9300);
    }

    @After
    public void after() {
        elastic.close();
    }

    @Test
    public void should_return_one() throws Exception {
        Generator generator = new Generator();
        ArrayList<ProductEntity> data = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            data.add(Generator.generate("product " + i, "description " + i, "category " + i, "http://site/product" + i));
        }
        indexProducts(Observable.from(data), "product" + indexSuffix, "guns");

        ListenableActionFuture<SearchResponse> query1 = runSearch("product 1", "category 1", 0);
        SearchResponse searchResponse1 = query1.get();
        System.out.println(searchResultToJson(searchResponse1));

        SearchHits hits1 = searchResponse1.getHits();
        Assert.assertEquals("Must match exactly 1", 1, hits1.getTotalHits());
    }

    @Test
    public void should_return_zero() throws Exception {
        Generator generator = new Generator();
        ArrayList<ProductEntity> data = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            data.add(Generator.generate("product " + i, "description " + i, "category " + i, "http://site/product" + i));
        }
        indexProducts(Observable.from(data), "product" + indexSuffix, "guns");

        ListenableActionFuture<SearchResponse> query0 = runSearch("product 1", "category 66", 0);
        SearchResponse searchResponse0 = query0.get();
        System.out.println(searchResultToJson(searchResponse0));

        SearchHits hits0 = searchResponse0.getHits();
        Assert.assertEquals("Must match exactly 0", 0, hits0.getTotalHits());
    }

    @Test
    public void should_return_two() throws Exception {
        Generator generator = new Generator();
        ArrayList<ProductEntity> data = new ArrayList<>();

        data.add(Generator.generate("product 22 hello", "description 22 foo", "category 22", "http://site/product22foo"));
        data.add(Generator.generate("product 22 world", "description 22 bar", "category 22", "http://site/product22bar"));
        data.add(Generator.generate("product 33", "description 33", "category 33", "http://site/product33"));
        indexProducts(Observable.from(data), "product" + indexSuffix, "guns");

        ListenableActionFuture<SearchResponse> query2 = runSearch("product 22", "category 22", 0);
        SearchResponse searchResponse2 = query2.get();
        System.out.println(searchResultToJson(searchResponse2));

        SearchHits hits2 = searchResponse2.getHits();
        Assert.assertEquals("Must match exactly 0", 2, hits2.getTotalHits());
    }

    protected ListenableActionFuture<SearchResponse> runSearch(String searchKey, String category, int startFrom) {
        String indexSuffix = "";//"""-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        boolQueryBuilder.should(QueryBuilders.multiMatchQuery(searchKey, "productName^3", "description", "category"));
        boolQueryBuilder.filter(QueryBuilders.existsQuery("category"));
        boolQueryBuilder.must(QueryBuilders.matchQuery("category", category).type(MatchQueryBuilder.Type.PHRASE));

        System.out.println(boolQueryBuilder);

        Client client = elastic.getClient();
        SearchRequestBuilder searchRequestBuilder = client.prepareSearch("product" + indexSuffix);
        searchRequestBuilder.setQuery(boolQueryBuilder);
        searchRequestBuilder.setTypes("guns");
        searchRequestBuilder.setSearchType(SearchType.DEFAULT);
        searchRequestBuilder.setFetchSource(includeFields, null);
        searchRequestBuilder.setFrom(startFrom).setSize(30).setExplain(true);

        return searchRequestBuilder.execute();
    }

    private Subscription indexProducts(Observable<ProductEntity> products, String index, String type) {
        return elastic.index(products, index, type);
    }

    private static String searchResultToJson(SearchResponse searchResponse) {
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
}
