package com.naxsoft.dumyData;

import com.google.common.base.CaseFormat;
import com.naxsoft.entity.ProductEntity;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Iterator;

/**
 * Copyright NAXSoft 2015
 */
public class Generator {
    private static final Logger logger = LoggerFactory.getLogger(Generator.class);

    public ProductEntity generate(String productName, String description, String category, String url) throws IOException {
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
