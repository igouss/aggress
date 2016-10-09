package com.naxsoft.parsers.productParser;

import com.naxsoft.entity.ProductEntity;
import com.naxsoft.entity.WebPageEntity;
import io.vertx.core.eventbus.Message;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
class EllwoodeppsRawProductParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(EllwoodeppsRawProductParser.class);
    private static final Map<String, String> mapping = new HashMap<>();
    private static final Pattern pricePattern = Pattern.compile("\\$((\\d+|,)+\\.\\d+)");

    static {
        mapping.put("firearm", "firearm");
        mapping.put("Ammo", "ammo");
        mapping.put("Archery Accessories", "misc");
        mapping.put("Barrels", "misc");
        mapping.put("Bayonets", "misc");
        mapping.put("Binoculars", "misc");
        mapping.put("Bipods", "optic");
        mapping.put("Books, Manuals and Videos", "misc");
        mapping.put("Chokes", "misc");
        mapping.put("Firearm Cleaning &Tools ", "misc");
        mapping.put("Grips", "misc");
        mapping.put("Gun Cases", "misc");
        mapping.put("Holsters", "misc");
        mapping.put("Knives", "misc");
        mapping.put("Magazines", "misc");
        mapping.put("Militaria", "misc");
        mapping.put("Misc Gun Parts", "misc");
        mapping.put("Misc Hunting Accessories", "misc");
        mapping.put("Reloading", "reload");
        mapping.put("Scopes & Mounts", "optic");
        mapping.put("Shooting Accessories", "misc");
        mapping.put("Stocks", "misc");
    }

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

            if (!document.select(".firearm-links-sold").isEmpty()) {
                return Observable.empty();
            }

            String productName = document.select(".product-name span").text();
            LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());

            ProductEntity product;
            try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
                jsonBuilder.startObject();
                jsonBuilder.field("url", webPageEntity.getUrl());
                jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
                jsonBuilder.field("productName", productName);

                jsonBuilder.field("regularPrice", parsePrice(document.select(".price").text()));

                Iterator<Element> labels = document.select("th.label").iterator();
                Iterator<Element> values = document.select("td.data").iterator();

                while (labels.hasNext()) {
                    String specName = labels.next().text();
                    String specValue = values.next().text();
                    if (!specValue.isEmpty()) {
                        jsonBuilder.field(specName, specValue);
                    }
                }

                jsonBuilder.field("category", getNormalizedCategories(webPageEntity));
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
        String s = mapping.get(webPageEntity.getCategory());
        if (null != s) {
            return s.split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().contains("ellwoodepps.com") && webPage.getType().equals("productPageRaw");
    }

    @Override
    public void start() throws Exception {
        super.start();
        vertx.eventBus().consumer("ellwoodepps.com/productPageRaw", (Message<WebPageEntity> event) ->
                parse(event.body()).subscribe(message -> vertx.eventBus().publish("productParseResult", message), err -> LOGGER.error("Failed to parse", err)));
    }
}
