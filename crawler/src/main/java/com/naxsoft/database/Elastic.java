//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.google.gson.Gson;
import com.naxsoft.crawler.AsyncFetchClient;
import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.ProductEntity;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Elastic {
    TransportClient client = null;
    private Logger logger;

    public void setup() {
        this.logger = LoggerFactory.getLogger(this.getClass());
        Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").put("client.transport.sniff", true).build();
        this.client = new TransportClient(settings);
        this.client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));

        while (true) {
            this.logger.info("Waiting for elastic to connect to a node...");
            int connectedNodes = this.client.connectedNodes().size();
            if (0 != connectedNodes) {
                this.logger.info("Connection established");
                break;
            }
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(5L));
            } catch (InterruptedException e) {
                this.logger.error("Thread sleep failed", e);
            }
        }
    }

    public void tearDown() {
        this.client.close();
    }

    public Client getClient() {
        return this.client;
    }


    public void index(Observable<ProductEntity> products, String index, String type) {
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();


        products.toList().subscribe(list -> {
            int i = 0;
            for (ProductEntity p : list) {
                try {
                    XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
                    jsonBuilder.startObject();
                    IndexRequestBuilder request = client.prepareIndex(index, type, "" + p.getUrl());
                    request.setSource(p.getJson());
                    request.setOpType(IndexRequest.OpType.INDEX);
                    bulkRequestBuilder.add(request);
                    if ((++i % 200) == 0) {
                        BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
                        if (bulkResponse.hasFailures()) {
                            this.logger.error("Failed to index products:" + bulkResponse.buildFailureMessage());
                        } else {
                            this.logger.info("Successfully indexed " + bulkResponse.getItems().length + " in " + bulkResponse.getTookInMillis() + "ms");
                        }
                    }
                } catch (IOException e) {
                    logger.error("Failed to create JSON generator");
                }
            }
            if (bulkRequestBuilder.numberOfActions() != 0) {
                BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
                if (bulkResponse.hasFailures()) {
                    this.logger.error("Failed to index products:" + bulkResponse.buildFailureMessage());
                } else {
                    this.logger.info("Successfully indexed " + bulkResponse.getItems().length + " in " + bulkResponse.getTookInMillis() + "ms");
                }
            }
        });

    }

    public String getIndex(String index, String type) throws IOException {
        FetchClient fetchClient = new FetchClient();
        return fetchClient.get("http://127.0.0.1:9200/" + index + "/" + type + "/_mapping?pretty=true").body();
    }

    public Integer createIndex(String index, String type, String indexSuffix) throws IOException, ExecutionException, InterruptedException {
        String resourceName = "/elastic." + index + "." + type + ".index.json";
        String newIndexName = index + indexSuffix;
        logger.info("Creating index " + newIndexName + " type " + type + " from " + resourceName);
        InputStream resourceAsStream = this.getClass().getResourceAsStream(resourceName);
        String indexContent = IOUtils.toString(resourceAsStream);


        String url = "http://127.0.0.1:9200/" + newIndexName;

        AsyncFetchClient<Integer> client = new AsyncFetchClient<>();
        Future<Integer> result = client.post(url, indexContent, new AsyncCompletionHandler<Integer>() {
            @Override
            public Integer onCompleted(Response response) throws Exception {
                int statusCode = response.getStatusCode();
                if (statusCode != 200) {
                    logger.error("Error creating index: " + response.getResponseBody());
                }

                return statusCode;
            }
        });
        Integer rc = result.get();
        client.close();
        return rc;
    }
}
