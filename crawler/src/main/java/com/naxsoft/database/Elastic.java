//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.crawler.FetchClient;
import com.naxsoft.entity.ProductEntity;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class Elastic {
    TransportClient client = null;
    private Logger logger;

    public void setup() {
        this.logger = LoggerFactory.getLogger(this.getClass());
        Settings settings = ImmutableSettings.settingsBuilder().put("cluster.name", "elasticsearch").put("client.transport.sniff", true).build();
        this.client = new TransportClient(settings);
        this.client.addTransportAddress(new InetSocketTransportAddress("localhost", 9300));

        while(true) {
            this.logger.info("Waiting for elastic to connect to a node...");
            int connectedNodes = this.client.connectedNodes().size();
            if(0 != connectedNodes) {
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


    public void index(Iterable<ProductEntity> products, String index, String type)  {
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk();
        int i = 0;

        for (ProductEntity p : products) {

            try {
                XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
                jsonBuilder.startObject();
                IndexRequestBuilder request = client.prepareIndex(index, type, "" + p.getId());
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
        if(bulkRequestBuilder.numberOfActions() != 0) {
            BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
            if(bulkResponse.hasFailures()) {
                this.logger.error("Failed to index products:" + bulkResponse.buildFailureMessage());
            } else {
                this.logger.info("Successfully indexed " + bulkResponse.getItems().length + " in " + bulkResponse.getTookInMillis() + "ms");
            }
        }
    }

    public String getIndex(String index, String type) throws IOException {
        FetchClient fetchClient = new FetchClient();
        return fetchClient.get("http://127.0.0.1:9200/" + index + "/" + type + "/_mapping?pretty=true").body();
    }

    public String createIndex(String index, String type, String indexSuffix) throws IOException {
        String resourceName = "/elastic." + index + "." + type + ".index.json";
        String newIndexName = index + indexSuffix;
        logger.info("Creating index " + newIndexName + " type " + type + " from " + resourceName);
        InputStream resourceAsStream = this.getClass().getResourceAsStream(resourceName);
        String indexContent = IOUtils.toString(resourceAsStream);
        FetchClient fetchClient = new FetchClient();
        return fetchClient.put("http://127.0.0.1:9200/" + newIndexName, indexContent);
    }
}
