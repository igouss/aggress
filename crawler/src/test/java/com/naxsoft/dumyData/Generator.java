package com.naxsoft.dumyData;

import com.naxsoft.entity.ProductEntity;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Copyright NAXSoft 2015
 */
public class Generator {
    private static final Logger logger = LoggerFactory.getLogger(Generator.class);

    public static ProductEntity generate(String productName, String description, String category, String url) throws IOException {
        XContentBuilder jsonBuilder = XContentFactory.jsonBuilder();
        jsonBuilder.startObject();
        jsonBuilder.field("url", url);
        jsonBuilder.field("productName", productName);
        jsonBuilder.field("description", description);
        jsonBuilder.field("category", category);
        jsonBuilder.endObject();

        ProductEntity product = new ProductEntity();
        product.setUrl(url);
        product.setJson(jsonBuilder.string());
        product.setWebpageId(42);

        return  product;
    }
}
