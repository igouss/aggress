import com.naxsoft.TestHandler
import org.elasticsearch.action.search.SearchRequestBuilder
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.search.SearchType
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.ImmutableSettings
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.SearchHit
import org.slf4j.LoggerFactory
import ratpack.groovy.template.MarkupTemplateModule
import ratpack.session.SessionModule
import ratpack.thymeleaf.ThymeleafModule

import java.nio.file.Paths
import java.util.concurrent.TimeUnit

import static ratpack.groovy.Groovy.ratpack
import static ratpack.jackson.Jackson.json
import static ratpack.thymeleaf.Template.thymeleafTemplate

def html = "text/html"

//
def logger = LoggerFactory.getLogger(this.getClass());
Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").put("client.transport.sniff", true).build();
def client = new TransportClient(settings);
client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));

while (true) {
    logger.info("Waiting for elastic to connect to a node...");
    int connectedNodes = client.connectedNodes().size();
    if (0 != connectedNodes) {
        logger.info("Connection established");
        break;
    }
    try {
        Thread.sleep(TimeUnit.SECONDS.toMillis(5L));
    } catch (InterruptedException e) {
        logger.error("Thread sleep failed", e);
    }


}

ratpack {
    bindings {
//        ConfigData configData = ConfigData.of().sysProps().build()
//        moduleConfig(new AssetPipelineModule(), configData.get(AssetPipelineModule.Config))
        // example of registering a module
        add(new SessionModule())
        add(new TestHandler())
        module(new MarkupTemplateModule())
        module(new ThymeleafModule())
    }

    serverConfig {
        port 8080
        baseDir(Paths.get(System.getProperty("basedir")))
        development true
    }

    handlers {
        get {



//            def map = new HashMap<String, String>()
//            map.put("user", "Iouri")
//            render thymeleafTemplate(map, "templates\\myTemplate", html)
        }
        get("hello/:name") {
            //render "Hello $pathTokens.name!"

            def name = pathTokens.name
            render json("Hello $name".toString())
        }

        get("search") {
            def searchKey = request.getQueryParams().get("searchKey")
            def startFrom = 0

            def query = '"aggs":{' +
                    '    "dedup" : {' +
                    '      "terms":{' +
                    '        "field": "url"' +
                    '       },' +
                    '       "aggs":{' +
                    '         "dedup_docs":{' +
                    '           "top_hits":{' +
                    '             "size":1' +
                    '           }' +
                    '         }' +
                    '       }    ' +
                    '    }' +
                    '  }'

            if (request.getQueryParams().containsKey("startFrom")) {
                startFrom = Integer.parseInt(request.getQueryParams().containsKey("startFrom"))
            }

            QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("*"+ searchKey +"*");
            SearchRequestBuilder searchRequestBuilder = client.prepareSearch("product-2015-24-12");
            searchRequestBuilder.setTypes("guns");
            searchRequestBuilder.setSearchType(SearchType.DEFAULT);
            searchRequestBuilder.setQuery(queryBuilder);
            searchRequestBuilder.setFrom(startFrom).setSize(60).setExplain(true);

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
            String result= builder.toString();


            render(result)

//            if (response != null) {
//                for (SearchHit hit : response.getHits()) {
//                }
//            }

//            def name = pathTokens.name
//            render json("Hello $name".toString())
        }



        get("api", TestHandler)
        post("api", TestHandler)
        delete("api", TestHandler)
        put("api", TestHandler)

        fileSystem "public", { f -> f.files() }
    }
}
