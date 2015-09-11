package web
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.search.SearchHit

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery

class SearchController {

    def index() {
    }

    def search() {
        TransportClient client = servletContext.getAttribute("elastic")


        SearchResponse searchResponse = client.prepareSearch()
                .setQuery(matchAllQuery())
                .setFrom(0).setSize(60).setExplain(true)
                .execute()
                .actionGet();


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
        String result= builder.toString();

        [ json: "hello" ]
    }
}
