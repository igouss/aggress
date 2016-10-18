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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class ProphetriverPawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WestrifleProductRawParser.class);
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");
    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("Rifles", "firearm");
        mapping.put("Shotguns", "firearm");
        mapping.put("Handguns", "firearm");
        mapping.put("Ammunition", "ammo");
        mapping.put("Reloading Equipment", "reload");
        mapping.put("Reloading Components", "reload");
        mapping.put("Rifle Scopes", "optic");
        mapping.put("Optics Accessories", "optic");
        mapping.put("Other Optics", "optic");
    }

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


            String productName = document.select(".BlockContent > h2").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());
            String category = document.select("#ProductBreadcrumb > ul > li:nth-child(2) > a").text();

            ProductEntity product;
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", webPageEntity.getUrl());
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
                jsonBuilder.field("productName", productName);
                String img = document.select(".ProductThumbImage img").attr("abs:src");
                if (!img.contains("DefaultProductImageCustom.jpg")) {
                    jsonBuilder.field("productImage", img);
                }
                jsonBuilder.field("description", document.select(".ProductDescriptionContainer").text());
                jsonBuilder.field("regularPrice", parsePrice(webPageEntity, document.select(".ProductPrice").text()));
                jsonBuilder.field("category", getNormalizedCategories(category));
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
     * @param category
     * @return
     */
    private String[] getNormalizedCategories(String category) {
        if (mapping.containsKey(category)) {
            return mapping.get(category).split(",");
        }
        LOGGER.warn("Unknown category: {}", category);
        return new String[]{"misc"};
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("prophetriver.com") && webPage.getType().equals("productPageRaw");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("prophetriver.com/productPageRaw", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("productParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
