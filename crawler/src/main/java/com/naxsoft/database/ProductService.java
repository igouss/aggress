//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.entity.ProductEntity;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest.OpType;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class ProductService {
    private final Logger logger;
    private Elastic elastic;
    private Database database;

    public ProductService(Elastic elastic, Database database) {
        this.elastic = elastic;
        this.database = database;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public void save(Set<ProductEntity> products) {
        try {
            Session session = this.database.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();
            int i = 0;
            Iterator client = products.iterator();

            while(client.hasNext()) {
                ProductEntity bulkRequest = (ProductEntity)client.next();
                session.save(bulkRequest);
                ++i;
                if(i % 20 == 0) {
                    session.flush();
                    session.clear();
                }
            }

            session.flush();
            session.clear();
            tx.commit();
            session.close();
            Client var13 = this.elastic.getClient();
            BulkRequestBuilder bulkRequestBuilder = var13.prepareBulk();
            i = 0;

            for (ProductEntity p : products) {
                XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
                jsonBuilder.startObject();
                IndexRequestBuilder request = var13.prepareIndex("product", "guns", "" + p.getId());
                request.setSource(p.getJson());
                request.setOpType(OpType.INDEX);
                bulkRequestBuilder.add(request);
                ++i;
                if (i % 100 == 0) {
                    BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
                    if (bulkResponse.hasFailures()) {
                        this.logger.error("Failed to index products:" + bulkResponse.buildFailureMessage());
                    } else {
                        this.logger.info("Successfully indexed " + bulkResponse.getItems().length + " in " + bulkResponse.getTookInMillis() + "ms");
                    }
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
        } catch (IOException e) {
            this.logger.error("Failed to save products", e);
        }

    }
}
