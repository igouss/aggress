package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * Copyright NAXSoft 2015
 */
class InternationalshootingsuppliesRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(HicalRawProductParser.class);

    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws ProductParseException {
        try {
            HashSet<ProductEntity> products = new HashSet<>();
            ProductEntity product = new ProductEntity();
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", webPageEntity.getUrl());
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));

                Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());

                String productName = document.select(".product_title").text();
                LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

                jsonBuilder.field("productName", productName);
                jsonBuilder.field("productImage", document.select(".product .wp-post-image").attr("abs:src"));

                jsonBuilder.field("regularPrice", document.select("meta[itemprop=price]").attr("content"));

                jsonBuilder.field("description", document.select("#tab-description").text().replace("Product Description", ""));
                jsonBuilder.field("category", webPageEntity.getCategory().split(","));
                jsonBuilder.endObject();
                product.setUrl(webPageEntity.getUrl());
                product.setWebpageId(webPageEntity.getId());
                product.setJson(jsonBuilder.string());
            }
            products.add(product);
            return products;
        } catch (Exception e) {
            throw new ProductParseException(e);
        }
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("internationalshootingsupplies.com") && webPage.getType().equals("productPageRaw");
    }
}
