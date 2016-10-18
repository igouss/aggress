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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by elendal on 10/12/16.
 */
public class WolverinegtRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WolverinegtRawPageParser.class);
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    /**
     * @param price
     * @return
     */
    private static String parsePrice(WebPageEntity webPageEntity, String price) {
        Matcher matcher = pricePattern.matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        } else {
            LOGGER.error("failed to parse price {}, page {}", price, webPageEntity.getUrl());
            return price;
        }
    }

    @Override
    public Observable<ProductEntity> parse(WebPageEntity webPageEntity) {
        HashSet<ProductEntity> result = new HashSet<>();

        try {
            Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
            String productName = document.select(".entry-summary h1").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            if (!document.select(".stock.out-of-stock").isEmpty()) {
                return Observable.empty();
            }

            ProductEntity product;
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", webPageEntity.getUrl());
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
                jsonBuilder.field("productName", productName);
                jsonBuilder.field("productImage", document.select("div.images img").attr("abs:src"));
//                jsonBuilder.field("manufacturer", document.select(".product-brand").text());
                jsonBuilder.field("category", getNormalizedCategories(webPageEntity));

                String price = document.select(".entry-summary  .price > .amount").text();
                if (price.isEmpty()) {
                    jsonBuilder.field("regularPrice", parsePrice(webPageEntity, document.select(".entry-summary .price del .amount").text()));
                    jsonBuilder.field("specialPrice", parsePrice(webPageEntity, document.select(".entry-summary .price ins .amount").text()));
                } else {
                    jsonBuilder.field("regularPrice", parsePrice(webPageEntity, price));
                }

                jsonBuilder.field("description", document.select(".entry-summary  .description").text());
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
        return webPageEntity.getCategory().split(",");
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("wolverinegt.ca") && webPage.getType().equals("productPageRaw");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("wolverinegt.ca/productPageRaw", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("productParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
