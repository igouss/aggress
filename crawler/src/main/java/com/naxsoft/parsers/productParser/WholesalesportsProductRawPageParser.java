package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import io.vertx.core.eventbus.Message;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class WholesalesportsProductRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WholesalesportsProductRawPageParser.class);
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    /**
     * @param price
     * @return
     */
    private static String parsePrice(String price) {
        Matcher matcher = pricePattern.matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        } else {
            LOGGER.error("failed to parse price {}", price);
            return price;
        }
    }

    @Override
    public Observable<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();

        try {

            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
            String productName = document.select("h1.product-name").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (2 == document.select("div.alert.negative").size()) {
                return Observable.empty();
            }

            ProductEntity product;
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", webPageEntity.getUrl());
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
                jsonBuilder.field("productName", productName);
                jsonBuilder.field("productImage", document.select(".productImagePrimaryLink img").attr("abs:src"));
                jsonBuilder.field("manufacturer", document.select(".product-brand").text());
                jsonBuilder.field("category", getNormalizedCategories(webPageEntity));
                if (document.select(".new .price-value").isEmpty()) {
                    Elements price = document.select(".current .price-value");
                    if (!price.isEmpty()) {
                        jsonBuilder.field("regularPrice", parsePrice(price.text()));
                    } else {
                        price = document.select("div.productDescription span.price-value");
                        jsonBuilder.field("regularPrice", parsePrice(price.text()));
                    }
                } else {
                    jsonBuilder.field("regularPrice", parsePrice(document.select(".old .price-value").text()));
                    jsonBuilder.field("specialPrice", parsePrice(document.select(".new .price-value").text()));
                }
                jsonBuilder.field("description", document.select(".summary").text());
                jsonBuilder.endObject();
                product = new ProductEntity(jsonBuilder.string(), webPageEntity.getUrl());
            }
            result.add(product);
        } catch (Exception e) {
            LOGGER.error("Failed to parse: {}", webPageEntity, e);
        }
        return Observable.from(result);
    }

    /**
     * @param webPageEntity
     * @return
     */
    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String category = webPageEntity.getCategory();
        if (null != category) {
            return category.split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("wholesalesports.com") && webPage.getType().equals("productPageRaw");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("wholesalesports.com/productPageRaw", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("productParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
