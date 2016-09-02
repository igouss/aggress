package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import io.vertx.core.eventbus.Message;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.HashSet;

/**
 * Copyright NAXSoft 2015
 */
class InternationalshootingsuppliesRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(HicalRawProductParser.class);

    @Override
    public Observable<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();

        try {
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
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Observable.from(result);
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("internationalshootingsupplies.com") && webPage.getType().equals("productPageRaw");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("internationalshootingsupplies.com/productPageRaw", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("productParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
