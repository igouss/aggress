//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.naxsoft.database;

import com.naxsoft.database.Database;
import com.naxsoft.database.Elasitic;
import com.naxsoft.entity.ProductEntity;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexRequest.OpType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProductService {
    private final Logger logger;
    private Elasitic elasitic;
    private Database database;

    public ProductService(Elasitic elasitic, Database database) {
        this.elasitic = elasitic;
        this.database = database;
        this.logger = LoggerFactory.getLogger(ProductService.class);
    }

    public void save(Set<ProductEntity> products) {
        try {
            Session e = this.database.getSessionFactory().openSession();
            Transaction tx = e.beginTransaction();
            int i = 0;
            Iterator client = products.iterator();

            while(client.hasNext()) {
                ProductEntity bulkRequest = (ProductEntity)client.next();
                e.save(bulkRequest);
                ++i;
                if(i % 20 == 0) {
                    e.flush();
                    e.clear();
                }
            }

            e.flush();
            e.clear();
            tx.commit();
            e.close();
            Client var13 = this.elasitic.getClient();
            BulkRequestBuilder var14 = var13.prepareBulk();
            i = 0;
            Iterator bulkResponse = products.iterator();

            while(bulkResponse.hasNext()) {
                ProductEntity p = (ProductEntity)bulkResponse.next();
                XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
                jsonBuilder.startObject();
                IndexRequestBuilder request = var13.prepareIndex("product", "guns", "" + p.getId());
                request.setSource(p.getJson());
                request.setOpType(OpType.INDEX);
                var14.add(request);
                ++i;
                if(i % 100 == 0) {
                    BulkResponse bulkResponse1 = (BulkResponse)var14.execute().actionGet();
                    if(bulkResponse1.hasFailures()) {
                        this.logger.error("Failed to index products");
                    } else {
                        this.logger.info("Successfully indexed " + bulkResponse1.getItems().length + " in " + bulkResponse1.getTookInMillis() + "ms");
                    }
                }
            }

            if(0 != i) {
                BulkResponse var15 = (BulkResponse)var14.execute().actionGet();
                if(var15.hasFailures()) {
                    this.logger.error("Failed to index products");
                } else {
                    this.logger.info("Successfully indexed " + var15.getItems().length + " in " + var15.getTookInMillis() + "ms");
                }
            }
        } catch (IOException var12) {
            this.logger.error("Failed to save products", var12);
        }

    }
}
