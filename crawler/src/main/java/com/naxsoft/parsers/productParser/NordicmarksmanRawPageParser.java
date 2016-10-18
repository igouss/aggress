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

public class NordicmarksmanRawPageParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(WestrifleProductRawParser.class);
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");
    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("AR-15", "firearm");
        mapping.put("Shotguns", "firearm");
        mapping.put("Air Rifles", "firearm");
        mapping.put("Barreled Actions", "firearm");
        mapping.put("Benchrest Rifles", "firearm");
        mapping.put("Biathlon Rifles", "firearm");
        mapping.put("Target Rifles", "firearm");
        mapping.put("Hunting Rifles", "firearm");
        mapping.put("MSR Rifles", "firearm");
        mapping.put("Laser Rifles", "firearm");
        mapping.put("Anschutz Rifle Stocks", "misc");
        mapping.put("Biathlon Rifle Stocks", "misc");
        mapping.put("Used/Demo Rifles", "firearm");
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

            if (document.select(".optionstyle").text().contains("This item is currently out of stock.")) {
                return Observable.empty();
            }

            String productName = document.select(".productname").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            ProductEntity product;
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", webPageEntity.getUrl());
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
                jsonBuilder.field("productName", productName);
                String img = document.select("img[name='altimage']").attr("abs:src");
                jsonBuilder.field("productImage", img);
                jsonBuilder.field("description", document.select(".producttabcontent").text());
                String originalPrice = document.select(".productpage .productmsrp strike").text();
                if (originalPrice.isEmpty()) {
                    originalPrice = document.select(".productpage .productunitprice").text();
                    jsonBuilder.field("regularPrice", parsePrice(webPageEntity, originalPrice));
                } else {
                    String specialPrice = document.select(".productpage .productunitprice").text();
                    originalPrice = document.select(".productpage .productmsrp strike").text();
                    jsonBuilder.field("regularPrice", parsePrice(webPageEntity, originalPrice));
                    jsonBuilder.field("specialPrice", parsePrice(webPageEntity, specialPrice));
                }
                jsonBuilder.field("category", getNormalizedCategories(webPageEntity.getCategory()));
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
        return webPage.getUrl().contains("nordicmarksman.com") && webPage.getType().equals("productPageRaw");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("nordicmarksman.com/productPageRaw", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("productParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
