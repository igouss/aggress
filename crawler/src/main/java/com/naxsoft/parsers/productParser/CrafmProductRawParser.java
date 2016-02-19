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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Copyright NAXSoft 2015
 */
public class CrafmProductRawParser extends AbstractRawPageParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrafmProductRawParser.class);
    private static final Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("Competition Accessories", "misc");
        mapping.put("Firearms", "firearm");
        mapping.put("Batteries", "misc");
        mapping.put("Magazines", "misc");
        mapping.put("Chronographs", "misc");
        mapping.put("Targets", "misc");
        mapping.put("Safes & Cases", "misc");
        mapping.put("Knives & Tools", "misc");
        mapping.put("Miscellaneous", "misc");
        mapping.put("Lights & Lasers", "misc");
        mapping.put("Books & DVD's", "misc");
        mapping.put("Mounts & Rings", "misc");
        mapping.put("Ammunition", "ammo");
        mapping.put("Parts & Accessories", "misc");
        mapping.put("Grips", "misc");
        mapping.put("Cleaning Products", "misc");
        mapping.put("Protection", "misc");
        mapping.put("Reloading", "reload");
        mapping.put("Scopes& Opticals", "optic");
        mapping.put("Clothing", "misc");
        mapping.put("LIQUIDATION", "misc");
        mapping.put("PROMOTIONS", "misc");
    }


    @Override
    public Set<ProductEntity> parse(WebPageEntity webPageEntity) throws Exception {
        HashSet<ProductEntity> result = new HashSet<>();

        Document document = Jsoup.parse(webPageEntity.getContent(), webPageEntity.getUrl());
        String productName = document.select(".product-essential .product-name").text();
        LOGGER.info("Parsing {}, page={}", productName, webPageEntity.getUrl());


        ProductEntity product = new ProductEntity();
        try (XContentBuilder jsonBuilder = XContentFactory.jsonBuilder()) {
            jsonBuilder.startObject();
            jsonBuilder.field("url", webPageEntity.getUrl());
            jsonBuilder.field("modificationDate", new Timestamp(System.currentTimeMillis()));
            jsonBuilder.field("productName", productName);
            String img = document.select(".product-image img").attr("src");
            jsonBuilder.field("productImage", img);
            jsonBuilder.field("regularPrice", parsePrice(document.select("#product_addtocart_form > div.product-shop > div:nth-child(4) > h2 > span").text()));
            jsonBuilder.field("description", document.select("div.short-description p[align=justify]").text());
            jsonBuilder.field("category", getNormalizedCategories(webPageEntity));
            jsonBuilder.endObject();
            product.setUrl(webPageEntity.getUrl());
            product.setJson(jsonBuilder.string());
        }
        product.setWebpageId(webPageEntity.getId());
        result.add(product);

        return result;

    }

    private String[] getNormalizedCategories(WebPageEntity webPageEntity) {
        String s = mapping.get(webPageEntity.getCategory());
        if (null != s) {
            return s.split(",");
        }
        LOGGER.warn("Unknown category: {} url {}", webPageEntity.getCategory(), webPageEntity.getUrl());
        return new String[]{"misc"};
    }

    /**
     * @param price
     * @return
     */
    private static String parsePrice(String price) {
        Matcher matcher = Pattern.compile("((\\d+|,)+\\.\\d+)").matcher(price);
        if (matcher.find()) {
            return matcher.group(1).replace(",", "");
        } else {
            LOGGER.error("failed to parse price {}", price);
            return price;
        }
    }


    @Override
    public boolean canParse(WebPageEntity webPage) {
        return webPage.getUrl().startsWith("http://www.crafm.com/") && webPage.getType().equals("productPageRaw");
    }
}
